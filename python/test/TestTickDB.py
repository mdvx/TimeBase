import unittest
import servertest
import dxapi

class TestTickDB(servertest.TBServerTest):

    streamKeys = [
        'bars1min', 'tradeBBO', 'l2'
    ]

    def test_isOpen(self):
        self.assertTrue(self.db.isOpen())

    def test_isReadOnly(self):
        self.assertFalse(self.db.isReadOnly())

    def test_createStream(self):
        key = self.streamKeys[1]
        try:
            with open('testdata/' + key + '.xml', 'r') as schemaFile:
                schema = schemaFile.read()

            options = dxapi.StreamOptions()
            options.name(key)
            options.description(key)
            options.scope = dxapi.StreamScope('DURABLE')
            options.distributionFactor = 1
            options.highAvailability = False
            options.polymorphic = False
            options.metadata(schema)

            self.db.createStream(key, options)

            stream = self.db.getStream(key)
            self.assertIsNotNone(stream)
            self.assertEqual(stream.key(), key)
            self.assertEqual(stream.name(), key)
            self.assertEqual(stream.distributionFactor(), 1)
            self.assertEqual(stream.description(), key)
            self.assertEqual(stream.highAvailability(), False)
            self.assertEqual(stream.polymorphic(), False)
            self.assertEqual(stream.periodicity(), 'IRREGULAR')
            self.assertIsNone(stream.location())
            self.assertIsNotNone(stream.metadata())
            self.assertEqual(str(stream.scope()), 'DURABLE')
            self.assertEqual(stream.unique(), False)
        finally:
            self.deleteStream(key)

    def test_createStreamQQL(self):
        key = self.streamKeys[1]
        try:
            self.createStreamQQL(key)

            stream = self.db.getStream(key)
            self.assertIsNotNone(stream)
            self.assertEqual(stream.key(), key)
            self.assertEqual(stream.name(), key)
            self.assertEqual(stream.distributionFactor(), 0)
            self.assertEqual(stream.description(), key)
            self.assertEqual(stream.highAvailability(), False)
            self.assertEqual(stream.polymorphic(), True)
            self.assertEqual(stream.periodicity(), 'IRREGULAR')
            self.assertIsNone(stream.location())
            self.assertIsNotNone(stream.metadata())
            self.assertEqual(str(stream.scope()), 'DURABLE')
            self.assertEqual(stream.unique(), False)
        finally:
            self.deleteStream(key)

    def test_listStreams(self):
        try:
            self.createStreams()
            keySet = set(self.streamKeys)
            keySet.add('events#')

            streams = self.db.listStreams()
            self.assertEqual(len(streams), len(keySet))

            for stream in streams:
                keySet.remove(stream.key())
            self.assertEqual(len(keySet), 0)
        finally:
            self.deleteStreams()

    def test_removeStream(self):
        key = 'l2'
        try:
            self.createStream(key)
            stream = self.db.getStream(key)
            self.assertIsNotNone(stream)

            stream.deleteStream()
            stream = self.db.getStream(key)
            self.assertIsNone(stream)
        finally:
            self.deleteStream(key)

    # helpers
    def createStreams(self):
        for key in self.streamKeys:
            self.createStream(key)

    def deleteStreams(self):
        for key in self.streamKeys:
            self.deleteStream(key)

if __name__ == '__main__':
    unittest.main()