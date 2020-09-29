import unittest
import servertest
import testutils
import dxapi

class TestLoader(servertest.TBServerTest):

    streamKeys = [
        'bars1min', 'tradeBBO', 'l2'
    ]

    def test_LoadFixed(self):
        key = self.streamKeys[0]
        try:
            stream = self.createStream(key, False)
            self.assertIsNotNone(stream)
            self.assertEqual(self.streamCount(key), 0)  # check stream is empty

            count = 12345
            loadCount = testutils.loadBars(stream, count)
            self.assertEqual(count, loadCount)
            readCount = self.streamCount(key)
            self.assertEqual(readCount, loadCount)
        finally:
            self.deleteStream(key)

    def test_LoadPolymorphic(self):
        key = self.streamKeys[1]
        try:
            stream = self.createStream(key, True)
            self.assertIsNotNone(stream)
            self.assertEqual(self.streamCount(key), 0)  # check stream is empty

            count = 12354
            loadCount = testutils.loadTradeBBO(stream, count)
            self.assertEqual(count * 2, loadCount)
            readCount = self.streamCount(key)
            self.assertEqual(readCount, loadCount)
        finally:
            self.deleteStream(key)

    def test_LoadL2(self):
        key = self.streamKeys[2]
        try:
            stream = self.createStream(key, True)
            self.assertIsNotNone(stream)
            self.assertEqual(self.streamCount(key), 0)  # check stream is empty

            count = 12543
            loadCount = testutils.loadL2(stream, count)
            self.assertEqual(count, loadCount)
            readCount = self.streamCount(key)
            self.assertEqual(readCount, loadCount)
        finally:
            self.deleteStream(key)

    def test_registerTypesAndEntities(self):
        key = self.streamKeys[1]
        loader = None
        cursor = None
        try:
            stream = self.createStream(key, True)
            self.assertIsNotNone(stream)
            self.assertEqual(self.streamCount(key), 0)  # check stream is empty

            loader = stream.createLoader(dxapi.LoadingOptions())
            self.assertIsNotNone(loader)

            message = dxapi.InstrumentMessage()

            # register types
            bboTypeId = loader.registerType('deltix.timebase.api.messages.BestBidOfferMessage')
            tradeTypeId = loader.registerType('deltix.timebase.api.messages.TradeMessage')

            # register entities
            instrument1 = loader.registerInstrument(dxapi.InstrumentType('EQUITY'), 'AAAA')
            instrument2 = loader.registerInstrument(dxapi.InstrumentIdentity(dxapi.InstrumentType.BOND, 'BBBB'))

            message.typeId = bboTypeId

            message.instrumentId = instrument1
            message.timestamp = 0
            loader.send(message)

            message.instrumentId = instrument2
            message.timestamp = 1
            loader.send(message)

            message.typeId = tradeTypeId

            message.instrumentId = instrument1
            message.timestamp = 2
            loader.send(message)

            message.instrumentId = instrument2
            message.timestamp = 3
            loader.send(message)

            loader.close()

            cursor = stream.createCursor(dxapi.SelectionOptions())
            self.assertIsNotNone(cursor)
            self.assertTrue(cursor.next())
            self.assertEqual(cursor.getMessage().typeName, 'deltix.timebase.api.messages.BestBidOfferMessage')
            self.assertEqual(cursor.getMessage().symbol, 'AAAA')
            self.assertEqual(cursor.getMessage().instrumentType, 'EQUITY')

            self.assertTrue(cursor.next())
            self.assertEqual(cursor.getMessage().typeName, 'deltix.timebase.api.messages.BestBidOfferMessage')
            self.assertEqual(cursor.getMessage().symbol, 'BBBB')
            self.assertEqual(cursor.getMessage().instrumentType, 'BOND')

            self.assertTrue(cursor.next())
            self.assertEqual(cursor.getMessage().typeName, 'deltix.timebase.api.messages.TradeMessage')
            self.assertEqual(cursor.getMessage().symbol, 'AAAA')
            self.assertEqual(cursor.getMessage().instrumentType, 'EQUITY')

            self.assertTrue(cursor.next())
            self.assertEqual(cursor.getMessage().typeName, 'deltix.timebase.api.messages.TradeMessage')
            self.assertEqual(cursor.getMessage().symbol, 'BBBB')
            self.assertEqual(cursor.getMessage().instrumentType, 'BOND')

        finally:
            if loader != None:
                loader.close()
            if cursor != None:
                cursor.close()
            self.deleteStream(key)


    # helpers

    def streamCount(self, key):
        cursor = self.db.executeQuery('SELECT count() as count FROM "' + key + '"')
        try:
            if cursor.next():
                return cursor.getMessage().COUNT
            else:
                return 0
        finally:
            if cursor != None:
                cursor.close()

if __name__ == '__main__':
    unittest.main()