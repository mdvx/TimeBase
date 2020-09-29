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

read = 0
startTime = time.time()

print('Start reading from ' + stream_name)
while cursor.next():
	message = cursor.getMessage()
	
	read = read + 1
	if (read % 1000000 == 0):
		print('Read ' + str(read) + ' messages')

endTime = time.time()
readTime = (endTime - startTime)
print(str(read) + ' messages')
print('Time: ' + str(readTime) + ' s')
print('Speed: ' + str(read / readTime) + ' msg/s')

