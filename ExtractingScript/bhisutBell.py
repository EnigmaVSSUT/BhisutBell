from urllib.request import urlopen
from bs4 import BeautifulSoup
import requests
import json
import timeit

start = timeit.default_timer()

#Your statements here

url = 'https://bhisutbell.firebaseio.com/notices.json'
latestNoticeUrl = 'https://bhisutbell.firebaseio.com/latestNotice.json'

html = urlopen("http://vssut.ac.in/").read()
soup = BeautifulSoup(html, "lxml")
soup.prettify()
element = soup.find("table", class_="table")

isTagType = type(element.findAll('td')[0]) #To exclude NavigableString from Tag. Initialized here!
latestNotice = requests.get(latestNoticeUrl)

for description in element.findAll('td'):
	value = description.contents[0]
	if type(value) is isTagType:
		if latestNotice.text.strip('/"') == value.contents[0]:	# get latest notice title and check with the first title
			print('up-to-date')
			break
		else:
			data['name'] = value.contents[0]
			data['url'] = "http://vssut.ac.in/" + value.attrs["href"]
			json_data = json.dumps(data)
			response = requests.post(url, data=json_data)
			print(json_data)
			notification_message = value.contents[0]
			notification_url = "http://vssut.ac.in/" + value.attrs["href"]
	else:
		data = {}
		data['date'] = value

#	updating the latestNotice
json_data = element.find('a').contents[0]
json_data = json.dumps(json_data)
response = requests.put(latestNoticeUrl, data=json_data)


stop = timeit.default_timer()
print(stop - start)

# send notification to everyone
header = {"Content-Type": "application/json; charset=utf-8",
          "Authorization": "Basic NWViNjZmOTUtMjIyZi00NDcyLTkxYmItNmMyMTM0YjA2ZWQ2"}

payload = {"app_id": "eaf72e14-0237-4e72-a0f6-abf7ca732db6",
           "included_segments": ["All"],
           "android_group": "Bhisut",
           "contents": {"en": notification_message},
           "url": notification_url,
           "large_icon": "http://icons.iconarchive.com/icons/fps.hu/free-christmas-flat/128/bell-icon.png"
           }
 
req = requests.post("https://onesignal.com/api/v1/notifications", headers=header, data=json.dumps(payload))
 
print(req.status_code, req.reason)