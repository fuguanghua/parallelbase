package me.fuguanghua.perftest.sequenced;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestDisruptor;
import me.fuguanghua.perftest.support.LongArrayEventHandler;
import me.fuguanghua.perftest.support.PerfTestUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

/**
 * <pre>
 * UniCast a series of items between 1 publisher and 1 event processor.
 *
 * +----+    +-----+
 * | P1 |--->| EP1 |
 * +----+    +-----+
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
public final class OneToOneSequencedLongArrayThroughputTest extends AbstractPerfTestDisruptor
{
    private static final int BUFFER_SIZE = 1024 * 1;
    private static final long ITERATIONS = 1000L * 1000L * 1L;
    private static final int ARRAY_SIZE = 2 * 1024;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(DaemonThreadFactory.INSTANCE);

    private static final EventFactory<long[]> FACTORY = new EventFactory<long[]>()
    {
        @Override
        public long[] newInstance()
        {
            return new long[ARRAY_SIZE];
        }
    };
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final RingBuffer<long[]> ringBuffer =
        createSingleProducer(FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());
    private final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
    private final LongArrayEventHandler handler = new LongArrayEventHandler();
    private final BatchEventProcessor<long[]> batchEventProcessor =
        new BatchEventProcessor<long[]>(ringBuffer, sequenceBarrier, handler);

    {
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
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
        long expectedCount = batchEventProcessor.getSequence().get() + ITERATIONS;
        handler.reset(latch, ITERATIONS);
        executor.submit(batchEventProcessor);
        long start = System.currentTimeMillis();

        final RingBuffer<long[]> rb = ringBuffer;

        for (long i = 0; i < ITERATIONS; i++)
        {
            long next = rb.next();
            long[] event = rb.get(next);
            for (int j = 0; j < event.length; j++)
            {
                event[j] = i;
            }
            rb.publish(next);
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * ARRAY_SIZE * 1000L) / (System.currentTimeMillis() - start);
        waitForEventProcessorSequence(expectedCount);
        batchEventProcessor.halt();

        PerfTestUtil.failIf(0, handler.getValue());

        return opsPerSecond;
    }

    private void waitForEventProcessorSequence(long expectedCount) throws InterruptedException
    {
        while (batchEventProcessor.getSequence().get() != expectedCount)
        {
            Thread.sleep(1);
        }
    }

    public static void main(String[] args) throws Exception
    {
        OneToOneSequencedLongArrayThroughputTest test = new OneToOneSequencedLongArrayThroughputTest();
        test.testImplementations();
    }
}
