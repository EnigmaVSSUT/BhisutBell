from urllib.request import urlopen
from bs4 import BeautifulSoup
import pandas as pd
import requests

# html = urlopen("http://vssut.ac.in/").read()
# soup = BeautifulSoup(html, "lxml")
# soup.prettify()

# element = soup.find("table", class_="table")
# for description in element.findAll('a'):
# 	print( "http://vssut.ac.in/" + description.attrs["href"] + " " + description.contents[0])

r = requests.get('https://bhisutbell.firebaseio.com/notices.json')
print(r)