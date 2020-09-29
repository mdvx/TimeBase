import dxapi

def writestream():
	db = dxapi.TickDb.createFromUrl("dxtick://localhost:8023")
	db.open(False)
	
	stream = db.getStream("l2")
	loader = stream.createLoader(dxapi.LoadingOptions())
	
	messages = []
	messages.append(dxapi.InstrumentMessage())
	messages[0].typeName = 'deltix.timebase.api.messages.L2Message'
	messages.append(dxapi.InstrumentMessage())
	messages[1].typeName = 'deltix.timebase.api.messages.Level2Message'
	
	actions = [dxapi.InstrumentMessage(), dxapi.InstrumentMessage()]
	actions[0].level = 1
	actions[0].isAsk = True
	actions[0].action = 'UPDATE'
	actions[0].price = 1.1
	actions[0].size = 10
	actions[0].numOfOrders = 3
	actions[1].level = 2
	actions[1].isAsk = False
	actions[1].action = 'DELETE'
	actions[1].price = 2.2
	actions[1].size = 20
	actions[1].numOfOrders = 5
	
	count = 10
	while count > 0:
		count -= 1
		
		# Initialize the fields of deltix.timebase.api.messages.L2Message:
		messages[0].symbol = 'DLTX'
		messages[0].instrumentType = 'EQUITY'
		messages[0].exchangeId = "NY4"
		messages[0].isImplied = True
		messages[0].isSnapshot = True
		messages[0].sequenceId = 1
		messages[0].originalTimestamp = 1373321607447  # = 2013-07-08 22:13:27.447 GMT
		messages[0].currencyCode = 1
		messages[0].sequenceNumber = 1
		messages[0].actions = actions
		loader.send(messages[0])
		print('[SENT]: ', str(vars(messages[0])))
		# Initialize the fields of deltix.timebase.api.messages.Level2Message:
		messages[1].symbol = 'DLTX'
		messages[1].instrumentType = 'EQUITY'
		messages[1].price = 1.5
		messages[1].size = 1.5
		messages[1].exchangeId = "NY4"
		messages[1].depth = 1
		messages[1].isAsk = True
		messages[1].action = 'INSERT'
		messages[1].isLast = True
		messages[1].numOfOrders = 1
		messages[1].originalTimestamp = 1373321607447  # = 2013-07-08 22:13:27.447 GMT
		messages[1].currencyCode = 1
		messages[1].sequenceNumber = 1
		loader.send(messages[1])
		print('[SENT]: ', str(vars(messages[1])))
	
	
	loader.close()
	db.close()

writestream()
