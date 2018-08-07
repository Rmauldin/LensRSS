from flask import Flask, request, Response, jsonify
import json
from newspaper import Article
from textblob import TextBlob
import numpy as np
from numpy.random import seed
seed(1)
#from tensorflow import set_random_seed
#set_random_seed(2)
import pickle
from sklearn import preprocessing
from keras.preprocessing.text import hashing_trick
from keras.models import Model, load_model
from keras.preprocessing.sequence import pad_sequences
print("Finished imports")


# takes in a list of texts
def setup_texts(texts, vocab_size, max_length):
    if(not isinstance(texts, list)):
        texts = [texts]
    x_test = [hashing_trick(d, vocab_size, hash_function='md5') for d in texts]
    x_test = pad_sequences(x_test, maxlen=max_length, padding='post')
    return x_test
	
def get_encoder():
    fileObj = open("models/encoder", 'rb')
    encoder = pickle.load(fileObj)
    fileObj.close()
    return encoder
	
def get_model(path):
    model = load_model(path)
    model.compile(loss='binary_crossentropy',
                    optimizer='adam',
                    metrics=['accuracy'])
    return model

print("Setting up model")
model = get_model("models/model.h5")
vocab_size = 2000
max_length = 100
encoder = get_encoder()
print("Finished model setup")


app = Flask(__name__)

@app.route('/', methods=['GET', 'POST'])
def home():
	return jsonify({'message':'at home'})

@app.route('/request-url-info', methods=['GET', 'POST'])
def get_info():
	url = request.form.get('url')
	print("Received " + url)
	article = Article(url)
	try:
		article.download()
		article.parse()
	except:
		print("Could not fetch URL: " + url)
		abort(404)
		return jsonify({'status':'failure'})
	blob = TextBlob(article.text)
	polarity = np.array([blob.sentiment.polarity])
	subjectivity = np.array([blob.sentiment.subjectivity])
	text = setup_texts([article.text], vocab_size, max_length)
	title = setup_texts([article.title], vocab_size, max_length)
	leaning = model.predict([text, title, polarity, subjectivity])
	leaning = encoder.inverse_transform(leaning)[0]
	print("Successful analysis")
	set_info = {'status':'successful',
					'text':article.text,
					'subjectivity':subjectivity[0],
					'polarity':polarity[0],
					'title':article.title,
					'image':article.top_image,
					'authors':article.authors,
					'leaning':leaning}
	json_info = jsonify(set_info)
	json_info.status_code = 200
	return json_info

if __name__ == '__main__':
	app.run(host='0.0.0.0', port='5000', threaded=True)
