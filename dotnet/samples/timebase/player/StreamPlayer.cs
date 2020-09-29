using System;
using System.IO;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.tickdb.pub;
using deltix.qsrv.hf.tickdb.pub.query;
using deltix.qsrv.hf.stream;
using deltix.util.time;
using deltix.data.stream;
using System.Threading;

namespace deltix.samples.timebase.basics {
    public class StreamPlayer {
        const bool log = true;

        private MessageSource src;
        private MessageChannel dest;
        private long? timeOffset = null;
        private long count = 1;

        public StreamPlayer(MessageSource src, MessageChannel dest)
        {
            this.src = src;
            this.dest = dest;
        }

        public void play()
        {
            try
            {                
                for (; ; )
                {
                    if (!src.next())
                    {
                        Console.WriteLine("Message file is FINISHED - terminating");
                        return;
                    }

                    InstrumentMessage msg = (InstrumentMessage) src.getMessage();
                    long mt = msg.timestamp;
                    long now = TimeKeeper.currentTime;

                    if (timeOffset.HasValue)
                    {
                        long timestamp = mt + timeOffset.Value;
                        int wait = (int) (timestamp - now);

                        if (wait > 2)
                            Thread.Sleep(wait);
                    }
                    else 
                        timeOffset = now - mt;

                    msg.timestamp = TimeKeeper.currentTime;
                    
                    if (log)
                        Console.WriteLine(count + ": " + msg);

                    dest.send(msg);
                    count++;
                }
            }
            catch (ThreadInterruptedException)
            {
                Console.WriteLine("Interrupted - terminating");
                timeOffset = null; // reset time offset so next time we re-calculate it
            }
        }

        public static void Main(string[] args) {
            const string dburl = "dxtick://localhost:8011";
            const string streamkey = "IQFeedTicks";
            const string msgf = @"S:\Shared\work\IQFeedTicks.qsmsg.gz";
            
            using (DXTickDB db = TickDBFactory.createFromUrl(dburl)) 
            {
                db.open (false);

                DXTickStream s = db.getStream(streamkey);

                using (TickLoader loader = s.createLoader(new LoadingOptions(true)))
                {
                    using (MessageReader2 reader = MessageReader2.createRaw(new java.io.File(msgf)))
                    {
                        StreamPlayer player = new StreamPlayer (reader, loader);

                        Thread t;
                                                
                        Console.WriteLine("Starting the player ...");
                        t = new Thread(player.play);
                        t.Start();

                        Thread.Sleep(5000);

                        Console.WriteLine("Pausing the player ...");
                        t.Interrupt();
                        t.Join();

                        Thread.Sleep(5000);

                        Console.WriteLine("Restarting the player ...");

                        t = new Thread(player.play);
                        t.Start();

                        Thread.Sleep(5000);

                        Console.WriteLine("Pausing the player ...");
                        t.Interrupt();
                        t.Join();
                    }
                }            
            }
        }
    }
}
