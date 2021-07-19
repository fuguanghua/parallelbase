package me.fuguanghua.perftest.queue;

import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestQueue;
import me.fuguanghua.perftest.support.ValueAdditionQueueProcessor;

import java.util.concurrent.*;

import static me.fuguanghua.perftest.support.PerfTestUtil.failIf;

/**
 * <pre>
 * UniCast a series of items between 1 publisher and 1 event processor.
 *
 * +----+    +-----+
 * | P1 |--->| EP1 |
 * +----+    +-----+
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
 * </pre>
 */
public final class OneToOneQueueThroughputTest extends AbstractPerfTestQueue
{
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 10L;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(DaemonThreadFactory.INSTANCE);
    private final long expectedResult = ITERATIONS * 3L;

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final BlockingQueue<Long> blockingQueue = new LinkedBlockingQueue<Long>(BUFFER_SIZE);
    private final ValueAdditionQueueProcessor queueProcessor =
        new ValueAdditionQueueProcessor(blockingQueue, ITERATIONS - 1);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount()
    {
        return 2;
    }

    @Override
    protected long runQueuePass() throws InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        queueProcessor.reset(latch);
        Future<?> future = executor.submit(queueProcessor);
        long start = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++)
        {
            blockingQueue.put(3L);
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);
        queueProcessor.halt();
        future.cancel(true);

        failIf(expectedResult, 0);

        return opsPerSecond;
    }

    public static void main(String[] args) throws Exception
    {
        OneToOneQueueThroughputTest test = new OneToOneQueueThroughputTest();
        test.testImplementations();
    }
}
