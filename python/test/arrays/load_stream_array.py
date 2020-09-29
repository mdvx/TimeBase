import dxapi
import sys
import time
import random

rand_str = lambda n: ''.join([random.choice('qo3io23jh334h3oiuh4oih3ozn.') for i in range(n)])

tb_url = 'dxtick://localhost:8023'
stream_name = 'arrays'

db = dxapi.TickDb.createFromUrl(tb_url)
db.open(False)

print('Connected to ' + tb_url)

stream = db.getStream(stream_name)
loader = stream.createLoader(dxapi.LoadingOptions())
tradeMessage = dxapi.InstrumentMessage()

print('Start loading to ' + stream_name)
loaded = 0

buy_sell = ['BUY', 'SELL']

obj1 = dxapi.InstrumentMessage()
obj1.typeName = 'InnerObject'
obj1.el1 = 1.2
obj1.el2 = 'asdf'
obj1.el3 = 'BUY'

obj2 = dxapi.InstrumentMessage()
obj2.typeName = 'InnerObject2'
obj2.field1 = 4.5
obj2.field2 = '123r23rsss'

for i in range(1000000):
	tradeMessage.typeName = 'deltix.timebase.api.messages.TradeMessage'
	tradeMessage.instrumentType = 'EQUITY'
	tradeMessage.symbol = 'AAA'
	tradeMessage.timestamp = i * 1000000000
	
	tradeMessage.originalTimestamp = 0
	tradeMessage.currencyCode = 999
	tradeMessage.sequenceNumber = 0
	tradeMessage.exchangeId = None
	tradeMessage.price = 10.0 + i * 2.2
	tradeMessage.size = 20.0 + i * 3.3
	tradeMessage.condition = 'CONDITION'
	tradeMessage.aggressorSide = 'BUY'
	tradeMessage.netPriceChange = 30.0 + i * 4.4
	tradeMessage.eventType = 'TRADE'
	
	tradeMessage.array_of_ints = []
	array_len = random.randint(1, 100)
	for j in range(array_len):
		tradeMessage.array_of_ints.append(random.randint(-10000, 10000))
		
	tradeMessage.array_of_enums = []
	array_len = random.randint(1, 10)
	for j in range(array_len):
		tradeMessage.array_of_enums.append(buy_sell[random.randint(0,1)])
	
	if i % 2 == 0:
		obj1.el1 = i / 3
		obj1.el2 = 'asdf' + str(i)
		tradeMessage.object1 = obj1
	else:
		obj2.field1 = i / 4
		obj2.field2 = 'asdfwwww' + str(i + 2)
		tradeMessage.object1 = obj2
	
	loader.send(tradeMessage)
	
	loaded = loaded + 1
	if loaded % 100000 == 0:
		print('Loaded ' + str(loaded) + ' messages')

loader.close()

print('Loading finished')




