package me.fuguanghua.perftest.sequenced;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestDisruptor;
import me.fuguanghua.perftest.support.LongArrayEventHandler;
import me.fuguanghua.perftest.support.LongArrayPublisher;
import me.fuguanghua.perftest.support.MultiBufferBatchEventProcessor;

import java.util.concurrent.*;

/**
 * <pre>
 *
 * Sequence a series of events from multiple publishers going to one event processor.
 *
 * Disruptor:
 * ==========
 *             track to prevent wrap
 *             +--------------------+
 *             |                    |
 *             |                    |
 * +----+    +====+    +====+       |
 * | P1 |--->| RB |--->| SB |--+    |
 * +----+    +====+    +====+  |    |
 *                             |    v
 * +----+    +====+    +====+  | +----+
 * | P2 |--->| RB |--->| SB |--+>| EP |
 * +----+    +====+    +====+  | +----+
 *                             |
 * +----+    +====+    +====+  |
 * | P3 |--->| RB |--->| SB |--+
 * +----+    +====+    +====+
 *
 * P1 - Publisher 1
 * P2 - Publisher 2
 * P3 - Publisher 3
 * RB - RingBuffer
 * SB - SequenceBarrier
 * EP - EventProcessor
 *
 * </pre>
 */
public final class ThreeToThreeSequencedThroughputTest extends AbstractPerfTestDisruptor
{
    private static final int NUM_PUBLISHERS = 3;
    private static final int ARRAY_SIZE = 3;
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 180L;
    private final ExecutorService executor =
        Executors.newFixedThreadPool(NUM_PUBLISHERS + 1, DaemonThreadFactory.INSTANCE);
    private final CyclicBarrier cyclicBarrier = new CyclicBarrier(NUM_PUBLISHERS + 1);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private final RingBuffer<long[]>[] buffers = new RingBuffer[NUM_PUBLISHERS];
    private final SequenceBarrier[] barriers = new SequenceBarrier[NUM_PUBLISHERS];
    private final LongArrayPublisher[] valuePublishers = new LongArrayPublisher[NUM_PUBLISHERS];

    private final LongArrayEventHandler handler = new LongArrayEventHandler();
    private final MultiBufferBatchEventProcessor<long[]> batchEventProcessor;

    private static final EventFactory<long[]> FACTORY = new EventFactory<long[]>()
    {
        @Override
        public long[] newInstance()
        {
            return new long[ARRAY_SIZE];
        }
    };

    {
        for (int i = 0; i < NUM_PUBLISHERS; i++)
        {
            buffers[i] = RingBuffer.createSingleProducer(FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());
            barriers[i] = buffers[i].newBarrier();
            valuePublishers[i] = new LongArrayPublisher(
                cyclicBarrier,
                buffers[i],
                ITERATIONS / NUM_PUBLISHERS,
                ARRAY_SIZE);
        }

        batchEventProcessor = new MultiBufferBatchEventProcessor<long[]>(buffers, barriers, handler);

        for (int i = 0; i < NUM_PUBLISHERS; i++)
        {
            buffers[i].addGatingSequences(batchEventProcessor.getSequences()[i]);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount()
    {
        return 4;
    }

    @Override
    protected long runDisruptorPass() throws Exception
    {
        final CountDownLatch latch = new CountDownLatch(1);
        handler.reset(latch, ITERATIONS);

        Future<?>[] futures = new Future[NUM_PUBLISHERS];
        for (int i = 0; i < NUM_PUBLISHERS; i++)
        {
            futures[i] = executor.submit(valuePublishers[i]);
        }
        executor.submit(batchEventProcessor);

        long start = System.currentTimeMillis();
        cyclicBarrier.await();

        for (int i = 0; i < NUM_PUBLISHERS; i++)
        {
            futures[i].get();
        }

        latch.await();

        long opsPerSecond = (ITERATIONS * 1000L * ARRAY_SIZE) / (System.currentTimeMillis() - start);
        batchEventProcessor.halt();

        return opsPerSecond;
    }

    public static void main(String[] args) throws Exception
    {
        new ThreeToThreeSequencedThroughputTest().testImplementations();
    }
}
