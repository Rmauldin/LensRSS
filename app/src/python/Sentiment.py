from textblob import TextBlob
from android.os import Bundle
from android.support.v7.app import AppCompatActivity

class Sentiment(String text):
	blob = None
	def __init__(self, text):
		blob = TextBlob(text)
	
	def getPolarity(self):
		return blob.sentiment.polarity
		
	def getSubjectivity(self):
		return blob.sentiment.subjectivity
	