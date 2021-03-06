package me.fuguanghua.perftest.sequenced;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestDisruptor;
import me.fuguanghua.perftest.support.Operation;
import me.fuguanghua.perftest.support.ValueEvent;
import me.fuguanghua.perftest.support.ValueMutationEventHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;
import static me.fuguanghua.perftest.support.PerfTestUtil.failIfNot;

/**
 * <pre>
 *
 * MultiCast a series of items between 1 publisher and 3 event processors.
 *
 *           +-----+
 *    +----->| EP1 |
 *    |      +-----+
 *    |
 * +----+    +-----+
 * | P1 |--->| EP2 |
 * +----+    +-----+
 *    |
 *    |      +-----+
 *    +----->| EP3 |
 *           +-----+
 *
 * Disruptor:
 * ==========
 *                             track to prevent wrap
 *             +--------------------+----------+----------+
 *             |                    |          |          |
 *             |                    v          v          v
 * +----+    +====+    +====+    +-----+    +-----+    +-----+
 * | P1 |--->| RB |<---| SB |    | EP1 |    | EP2 |    | EP3 |
 * +----+    +====+    +====+    +-----+    +-----+    +-----+
 *      claim      get    ^         |          |          |
 *                        |         |          |          |
 *                        +---------+----------+----------+
 *                                      waitFor
 *
 * P1  - Publisher 1
 * RB  - RingBuffer
 * SB  - SequenceBarrier
 * EP1 - EventProcessor 1
 * EP2 - EventProcessor 2
 * EP3 - EventProcessor 3
 *
 * </pre>
 */
public final class OneToThreeSequencedThroughputTest extends AbstractPerfTestDisruptor
{
    private static final int NUM_EVENT_PROCESSORS = 3;
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final long ITERATIONS = 1000L * 1000L * 100L;
    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_EVENT_PROCESSORS, DaemonThreadFactory.INSTANCE);

    private final long[] results = new long[NUM_EVENT_PROCESSORS];

    {
        for (long i = 0; i < ITERATIONS; i++)
        {
            results[0] = Operation.ADDITION.op(results[0], i);
            results[1] = Operation.SUBTRACTION.op(results[1], i);
            results[2] = Operation.AND.op(results[2], i);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final RingBuffer<ValueEvent> ringBuffer =
        createSingleProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());

    private final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

    private final ValueMutationEventHandler[] handlers = new ValueMutationEventHandler[NUM_EVENT_PROCESSORS];

    {
        handlers[0] = new ValueMutationEventHandler(Operation.ADDITION);
        handlers[1] = new ValueMutationEventHandler(Operation.SUBTRACTION);
        handlers[2] = new ValueMutationEventHandler(Operation.AND);
    }

    private final BatchEventProcessor<?>[] batchEventProcessors = new BatchEventProcessor[NUM_EVENT_PROCESSORS];

    {
        batchEventProcessors[0] = new BatchEventProcessor<ValueEvent>(ringBuffer, sequenceBarrier, handlers[0]);
        batchEventProcessors[1] = new BatchEventProcessor<ValueEvent>(ringBuffer, sequenceBarrier, handlers[1]);
        batchEventProcessors[2] = new BatchEventProcessor<ValueEvent>(ringBuffer, sequenceBarrier, handlers[2]);

        ringBuffer.addGatingSequences(
            batchEventProcessors[0].getSequence(),
            batchEventProcessors[1].getSequence(),
            batchEventProcessors[2].getSequence());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount()
    {
        return 4;
    }

    @Override
    protected long runDisruptorPass() throws InterruptedException
    {
        CountDownLatch latch = new CountDownLatch(NUM_EVENT_PROCESSORS);
        for (int i = 0; i < NUM_EVENT_PROCESSORS; i++)
        {
            handlers[i].reset(latch, batchEventProcessors[i].getSequence().get() + ITERATIONS);
            executor.submit(batchEventProcessors[i]);
        }

        long start = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++)
        {
            long sequence = ringBuffer.next();
            ringBuffer.get(sequence).setValue(i);
            ringBuffer.publish(sequence);
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);
        for (int i = 0; i < NUM_EVENT_PROCESSORS; i++)
        {
            batchEventProcessors[i].halt();
            failIfNot(results[i], handlers[i].getValue());
        }

        return opsPerSecond;
    }

    public static void main(String[] args) throws Exception
    {
        new OneToThreeSequencedThroughputTest().testImplementations();
    }
}
