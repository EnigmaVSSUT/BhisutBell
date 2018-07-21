from urllib.request import urlopen
from bs4 import BeautifulSoup
import pandas as pd
import requests
import json

url = 'https://bhisutbell.firebaseio.com/notices.json'

html = urlopen("http://vssut.ac.in/").read()
soup = BeautifulSoup(html, "lxml")
soup.prettify()

element = soup.find("table", class_="table")

isTagType = type(element.findAll('td')[0]) #	To exclude NavigableString from Tag. Initialized here!

for description in element.findAll('td'):
	
	value = description.contents[0]
	if type(value) is isTagType:
		data['name'] = value.contents[0]
		data['url'] ="http://vssut.ac.in/" + value.attrs["href"]
		json_data = json.dumps(data)
		print(json_data)
		response = requests.post(url, data=json_data)
	else:
		data = {}
		data['date'] = value
	