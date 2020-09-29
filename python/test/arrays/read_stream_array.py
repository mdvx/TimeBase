import dxapi
import sys
import time

tb_url = 'dxtick://localhost:8023'
stream_name = 'arrays'

db = dxapi.TickDb.createFromUrl(tb_url)
db.open(False)

print('Connected to ' + tb_url)

stream = db.getStream(stream_name)
cursor = stream.createCursor(dxapi.SelectionOptions())

print('Start reading from ' + stream_name)
while cursor.next():
	message = cursor.getMessage()
	print(message)




