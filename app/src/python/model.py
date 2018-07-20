from newspaper import Article
from textblob import TextBlob
import numpy as np
from numpy.random import seed
seed(1)
from tensorflow import set_random_seed
set_random_seed(2)
import pickle
from sklearn import preprocessing
from keras.preprocessing.text import hashing_trick
from keras.models import Model, load_model
from keras.preprocessing.sequence import pad_sequences

class KerasModel():
	
	def __init__(self):
		model = get_model("models/model.h5")
		
	def get_model(path) -> Model:
		model = load_model(path)
		model.compile(loss='binary_crossentropy',
                    optimizer='adam',
                    metrics=['accuracy'])
		return model
	
	def analyzeUrl(self, url: java.lang.String) -> java.lang.String:
		print("Analyzed URL")
		return "Woo"