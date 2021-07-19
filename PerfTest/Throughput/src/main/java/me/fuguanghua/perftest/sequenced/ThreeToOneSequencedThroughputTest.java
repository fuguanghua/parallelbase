package me.fuguanghua.perftest.sequenced;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestDisruptor;
import me.fuguanghua.perftest.support.ValueAdditionEventHandler;
import me.fuguanghua.perftest.support.ValueEvent;
import me.fuguanghua.perftest.support.ValuePublisher;

import java.util.concurrent.*;

import static com.lmax.disruptor.RingBuffer.createMultiProducer;

/**
 * <pre>
 *
 * Sequence a series of events from multiple publishers going to one event processor.
 *
 * +----+
 * | P1 |------+
 * +----+      |
 *             v
 * +----+    +-----+
 * | P1 |--->| EP1 |
 * +----+    +-----+
 *             ^
 * +----+      |
 * | P3 |------+
 * +----+
 *
 *
 * Disruptor:
 * ==========
 *             track to prevent wrap
 *             +--------------------+
 *             |                    |
 *             |                    v
 * +----+    +====+    +====+    +-----+
 * | P1 |--->| RB |<---| SB |    | EP1 |
 * +----+    +====+    +====+    +-----+
 *             ^   get    ^         |
 * +----+      |          |         |
 * | P2 |------+          +---------+
 * +----+      |            waitFor
 *             |
 * +----+      |
 * | P3 |------+
 * +----+
 *
 * P1  - Publisher 1
 * P2  - Publisher 2
 * P3  - Publisher 3
 * RB  - RingBuffer
 * SB  - SequenceBarrier
 * EP1 - EventProcessor 1
 *
 * </pre>
 */
public final class ThreeToOneSequencedThroughputTest extends AbstractPerfTestDisruptor
{
    private static final int NUM_PUBLISHERS = 3;
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 20L;
    private final ExecutorService executor =
        Executors.newFixedThreadPool(NUM_PUBLISHERS + 1, DaemonThreadFactory.INSTANCE);
    private final CyclicBarrier cyclicBarrier = new CyclicBarrier(NUM_PUBLISHERS + 1);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final RingBuffer<ValueEvent> ringBuffer =
        createMultiProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new BusySpinWaitStrategy());

    private final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
    private final ValueAdditionEventHandler handler = new ValueAdditionEventHandler();
    private final BatchEventProcessor<ValueEvent> batchEventProcessor =
        new BatchEventProcessor<ValueEvent>(ringBuffer, sequenceBarrier, handler);
    private final ValuePublisher[] valuePublishers = new ValuePublisher[NUM_PUBLISHERS];

    {
        for (int i = 0; i < NUM_PUBLISHERS; i++)
        {
            valuePublishers[i] = new ValuePublisher(cyclicBarrier, ringBuffer, ITERATIONS / NUM_PUBLISHERS);
        }

        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
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
        handler
            .reset(latch, batchEventProcessor.getSequence().get() + ((ITERATIONS / NUM_PUBLISHERS) * NUM_PUBLISHERS));

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

        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);
        batchEventProcessor.halt();

        return opsPerSecond;
    }

    public static void main(String[] args) throws Exception
    {
        new ThreeToOneSequencedThroughputTest().testImplementations();
    }
}
