import dxapi
import time
import sys
from datetime import datetime

def readstream():
	# Create timebase connection
	db = dxapi.TickDb.createFromUrl("dxtick://localhost:8023")
	
	try:
		# Open in read-write mode
		db.open(True)
		
		# Get the data stream
		stream = db.getStream("l2")
		
		# Create cursor using defined "ALL" subscription
		cursor = stream.select(0, dxapi.SelectionOptions(), None, None)
		
		# Iterate first 100 messages in available in cursor
		for num in range(0,100):
			if cursor.next():
				message = cursor.getMessage()
				print(toTimeString(message.timestamp) + ": " + str(message))
				if message.typeName == 'deltix.timebase.api.messages.L2Message':
					for action in message.actions:
						print('Action: ' + str(action))
		
		# cursor should be closed after use
		cursor.close()
	finally:
		# database connection should be closed
		db.close()

def toTimeString(timestamp):
	# Message timestamp is Epoch time in nanoseconds
	seconds = int(timestamp/1000000000)
	nanoseconds = int(timestamp % 1000000000)
	
	time = ""
	while nanoseconds > 0:
		r = nanoseconds % 1000
		if r > 0:
			time = str(r) + time
		nanoseconds = int(nanoseconds/1000)
	
	return str(datetime.utcfromtimestamp(seconds)) + ("." if len(time) > 0 else "") + time

readstream()