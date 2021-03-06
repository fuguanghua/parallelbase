package me.fuguanghua.perftest.raw;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestDisruptor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <pre>
 * UniCast a series of items between 1 publisher and 1 event processor.
 *
 * +----+    +-----+
 * | P1 |--->| EP1 |
 * +----+    +-----+
 *
 *
 * Queue Based:
 * ============
 *
 *        put      take
 * +----+    +====+    +-----+
 * | P1 |--->| Q1 |<---| EP1 |
 * +----+    +====+    +-----+
 *
 * P1  - Publisher 1
 * Q1  - Queue 1
 * EP1 - EventProcessor 1
 *
 *
 * Disruptor:
 * ==========
 *              track to prevent wrap
 *              +------------------+
 *              |                  |
 *              |                  v
 * +----+    +====+    +====+   +-----+
 * | P1 |--->| RB |<---| SB |   | EP1 |
 * +----+    +====+    +====+   +-----+
 *      claim      get    ^        |
 *                        |        |
 *                        +--------+
 *                          waitFor
 *
 * P1  - Publisher 1
 * RB  - RingBuffer
 * SB  - SequenceBarrier
 * EP1 - EventProcessor 1
 *
 * </pre>
 */
public final class OneToOneRawThroughputTest extends AbstractPerfTestDisruptor
{
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 200L;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(DaemonThreadFactory.INSTANCE);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final Sequencer sequencer = new SingleProducerSequencer(BUFFER_SIZE, new YieldingWaitStrategy());
    private final MyRunnable myRunnable = new MyRunnable(sequencer);

    {
        sequencer.addGatingSequences(myRunnable.sequence);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount()
    {
        return 2;
    }

    @Override
    protected long runDisruptorPass() throws InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        long expectedCount = myRunnable.sequence.get() + ITERATIONS;
        myRunnable.reset(latch, expectedCount);
        executor.submit(myRunnable);
        long start = System.currentTimeMillis();

        final Sequenced sequencer = this.sequencer;

        for (long i = 0; i < ITERATIONS; i++)
        {
            long next = sequencer.next();
            sequencer.publish(next);
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);
        waitForEventProcessorSequence(expectedCount);

        return opsPerSecond;
    }

    private void waitForEventProcessorSequence(long expectedCount) throws InterruptedException
    {
        while (myRunnable.sequence.get() != expectedCount)
        {
            Thread.sleep(1);
        }
    }

    private static class MyRunnable implements Runnable
    {
        private CountDownLatch latch;
        private long expectedCount;
        Sequence sequence = new Sequence(-1);
        private final SequenceBarrier barrier;

        MyRunnable(Sequencer sequencer)
        {
            this.barrier = sequencer.newBarrier();
        }

        public void reset(CountDownLatch latch, long expectedCount)
        {
            this.latch = latch;
            this.expectedCount = expectedCount;
        }

        @Override
        public void run()
        {
            long expected = expectedCount;
            long processed = -1;

            try
            {
                do
                {
                    processed = barrier.waitFor(sequence.get() + 1);
                    sequence.set(processed);
                }
                while (processed < expected);

                latch.countDown();
                sequence.setVolatile(processed);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        OneToOneRawThroughputTest test = new OneToOneRawThroughputTest();
        test.testImplementations();
    }
}
