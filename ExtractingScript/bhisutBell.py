from urllib.request import urlopen
from bs4 import BeautifulSoup
import requests
import json
import schedule
import time
import timeit
import datetime

def job():
	start = timeit.default_timer()

	url = 'https://bhisutbell.firebaseio.com/notices.json'
	latestNoticeUrl = 'https://bhisutbell.firebaseio.com/latestNotice.json'
	lastUpdated = 'https://bhisutbell.firebaseio.com/lastUpdated.json'

	html = urlopen("http://vssut.ac.in/").read()
	soup = BeautifulSoup(html, "lxml")
	soup.prettify()
	element = soup.find("table", class_="table")

	isTagType = type(element.findAll('td')[0]) #To exclude NavigableString from Tag. Initialized here!
	latestNotice = requests.get(latestNoticeUrl)

	notifications = []

	for description in element.findAll('td'):
		value = description.contents[0]
		if type(value) is isTagType:
			latestNoticeCleaned = latestNotice.text.strip() # removes escape sequence
			if latestNoticeCleaned.strip('/"') == value.contents[0].strip():	# get latest notice title and check with the first title
				print('up-to-date')
				break
			else:
				data['name'] = value.contents[0].strip()
				data['url'] = "http://vssut.ac.in/" + value.attrs["href"]
				json_data = json.dumps(data)
				response = requests.post(url, data=json_data)
				print(json_data)
				notifications.append(data)
		else:
			data = {}
			data['date'] = value

	#	updating the latestNotice
	json_data = element.find('a').contents[0].strip()
	json_data = json.dumps(json_data)
	response = requests.put(latestNoticeUrl, data=json_data)

	# send notification
	header = {"Content-Type": "application/json; charset=utf-8",
	          "Authorization": "Basic ****ask ss.saswatsahoo@gmail.com***"}
	for notification in notifications:

		payload = {"app_id": "eaf72e14-0237-4e72-a0f6-abf7ca732db6",
		           "included_segments": ["All"],
		           "android_group": "Bhisut",
		           "contents": {"en": notification["name"]},
		           "url":  notification["url"],
		           "large_icon": "https://firebasestorage.googleapis.com/v0/b/bhisutbell.appspot.com/o/notification%20(1).png?alt=media&token=396376b3-6c51-4cfc-80a3-a57f6237420f"
		           }
		 
		req = requests.post("https://onesignal.com/api/v1/notifications", headers=header, data=json.dumps(payload))
		print(req.status_code, req.reason)

	# update lastUpdated value
	json_data = json.dumps(str(datetime.datetime.now()))
	response = requests.put(lastUpdated, data=json_data)

	stop = timeit.default_timer()
	file = open('logs.txt', 'a')
	file.write("Time taken to run: " + str(stop-start) + " @ " + str(datetime.datetime.now()) + '\n')
	print("Time taken to run: " + str(stop-start) + " @ " + str(datetime.datetime.now()) + '\n\n')
	file.close()

schedule.every(15).minutes.do(job)
schedule.every().day.at("00:09").do(job)

while 1:
    schedule.run_pending()
    time.sleep(1)