package me.fuguanghua.perftest.queue;

import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestQueue;
import me.fuguanghua.perftest.support.Operation;
import me.fuguanghua.perftest.support.ValueMutationQueueProcessor;

import java.util.concurrent.*;

import static me.fuguanghua.perftest.support.PerfTestUtil.failIf;

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
 *
 * Queue Based:
 * ============
 *                 take
 *   put     +====+    +-----+
 *    +----->| Q1 |<---| EP1 |
 *    |      +====+    +-----+
 *    |
 * +----+    +====+    +-----+
 * | P1 |--->| Q2 |<---| EP2 |
 * +----+    +====+    +-----+
 *    |
 *    |      +====+    +-----+
 *    +----->| Q3 |<---| EP3 |
 *           +====+    +-----+
 *
 * P1  - Publisher 1
 * Q1  - Queue 1
 * Q2  - Queue 2
 * Q3  - Queue 3
 * EP1 - EventProcessor 1
 * EP2 - EventProcessor 2
 * EP3 - EventProcessor 3
 *
 * </pre>
 */
public final class OneToThreeQueueThroughputTest extends AbstractPerfTestQueue
{
    private static final int NUM_EVENT_PROCESSORS = 3;
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final long ITERATIONS = 1000L * 1000L * 1L;
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

    @SuppressWarnings("unchecked")
    private final BlockingQueue<Long>[] blockingQueues = new BlockingQueue[NUM_EVENT_PROCESSORS];

    {
        blockingQueues[0] = new LinkedBlockingQueue<Long>(BUFFER_SIZE);
        blockingQueues[1] = new LinkedBlockingQueue<Long>(BUFFER_SIZE);
        blockingQueues[2] = new LinkedBlockingQueue<Long>(BUFFER_SIZE);
    }

    private final ValueMutationQueueProcessor[] queueProcessors = new ValueMutationQueueProcessor[NUM_EVENT_PROCESSORS];

    {
        queueProcessors[0] = new ValueMutationQueueProcessor(blockingQueues[0], Operation.ADDITION, ITERATIONS - 1);
        queueProcessors[1] = new ValueMutationQueueProcessor(blockingQueues[1], Operation.SUBTRACTION, ITERATIONS - 1);
        queueProcessors[2] = new ValueMutationQueueProcessor(blockingQueues[2], Operation.AND, ITERATIONS - 1);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount()
    {
        return 4;
    }

    @Override
    protected long runQueuePass() throws InterruptedException
    {
        CountDownLatch latch = new CountDownLatch(NUM_EVENT_PROCESSORS);
        Future<?>[] futures = new Future[NUM_EVENT_PROCESSORS];
        for (int i = 0; i < NUM_EVENT_PROCESSORS; i++)
        {
            queueProcessors[i].reset(latch);
            futures[i] = executor.submit(queueProcessors[i]);
        }

        long start = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++)
        {
            final Long value = Long.valueOf(i);
            for (BlockingQueue<Long> queue : blockingQueues)
            {
                queue.put(value);
            }
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);
        for (int i = 0; i < NUM_EVENT_PROCESSORS; i++)
        {
            queueProcessors[i].halt();
            futures[i].cancel(true);
            failIf(queueProcessors[i].getValue(), -1);
        }

        return opsPerSecond;
    }

    public static void main(String[] args) throws Exception
    {
        new OneToThreeQueueThroughputTest().testImplementations();
    }
}
