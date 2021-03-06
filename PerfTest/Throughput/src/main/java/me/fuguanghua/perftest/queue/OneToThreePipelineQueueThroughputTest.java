package me.fuguanghua.perftest.queue;

import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestQueue;
import me.fuguanghua.perftest.support.FunctionQueueProcessor;
import me.fuguanghua.perftest.support.FunctionStep;

import java.util.concurrent.*;

import static me.fuguanghua.perftest.support.PerfTestUtil.failIf;

/**
 * <pre>
 *
 * Pipeline a series of stages from a publisher to ultimate event processor.
 * Each event processor depends on the output of the event processor.
 *
 * +----+    +-----+    +-----+    +-----+
 * | P1 |--->| EP1 |--->| EP2 |--->| EP3 |
 * +----+    +-----+    +-----+    +-----+
 *
 *
 * Queue Based:
 * ============
 *
 *        put      take        put      take        put      take
 * +----+    +====+    +-----+    +====+    +-----+    +====+    +-----+
 * | P1 |--->| Q1 |<---| EP1 |--->| Q2 |<---| EP2 |--->| Q3 |<---| EP3 |
 * +----+    +====+    +-----+    +====+    +-----+    +====+    +-----+
 *
 * P1  - Publisher 1
 * Q1  - Queue 1
 * EP1 - EventProcessor 1
 * Q2  - Queue 2
 * EP2 - EventProcessor 2
 * Q3  - Queue 3
 * EP3 - EventProcessor 3
 *
 * </pre>
 */
public final class OneToThreePipelineQueueThroughputTest extends AbstractPerfTestQueue
{
    private static final int NUM_EVENT_PROCESSORS = 3;
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final long ITERATIONS = 1000L * 1000L * 10L;
    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_EVENT_PROCESSORS, DaemonThreadFactory.INSTANCE);

    private static final long OPERAND_TWO_INITIAL_VALUE = 777L;
    private final long expectedResult;

    {
        long temp = 0L;
        long operandTwo = OPERAND_TWO_INITIAL_VALUE;

        for (long i = 0; i < ITERATIONS; i++)
        {
            long stepOneResult = i + operandTwo--;
            long stepTwoResult = stepOneResult + 3;

            if ((stepTwoResult & 4L) == 4L)
            {
                ++temp;
            }
        }

        expectedResult = temp;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final BlockingQueue<long[]> stepOneQueue = new LinkedBlockingQueue<long[]>(BUFFER_SIZE);
    private final BlockingQueue<Long> stepTwoQueue = new LinkedBlockingQueue<Long>(BUFFER_SIZE);
    private final BlockingQueue<Long> stepThreeQueue = new LinkedBlockingQueue<Long>(BUFFER_SIZE);

    private final FunctionQueueProcessor stepOneQueueProcessor =
        new FunctionQueueProcessor(FunctionStep.ONE, stepOneQueue, stepTwoQueue, stepThreeQueue, ITERATIONS - 1);
    private final FunctionQueueProcessor stepTwoQueueProcessor =
        new FunctionQueueProcessor(FunctionStep.TWO, stepOneQueue, stepTwoQueue, stepThreeQueue, ITERATIONS - 1);
    private final FunctionQueueProcessor stepThreeQueueProcessor =
        new FunctionQueueProcessor(FunctionStep.THREE, stepOneQueue, stepTwoQueue, stepThreeQueue, ITERATIONS - 1);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount()
    {
        return 4;
    }

    @Override
    protected long runQueuePass() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        stepThreeQueueProcessor.reset(latch);

        Future<?>[] futures = new Future[NUM_EVENT_PROCESSORS];
        futures[0] = executor.submit(stepOneQueueProcessor);
        futures[1] = executor.submit(stepTwoQueueProcessor);
        futures[2] = executor.submit(stepThreeQueueProcessor);

        long start = System.currentTimeMillis();

        long operandTwo = OPERAND_TWO_INITIAL_VALUE;
        for (long i = 0; i < ITERATIONS; i++)
        {
            long[] values = new long[2];
            values[0] = i;
            values[1] = operandTwo--;
            stepOneQueue.put(values);
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);

        stepOneQueueProcessor.halt();
        stepTwoQueueProcessor.halt();
        stepThreeQueueProcessor.halt();

        for (Future<?> future : futures)
        {
            future.cancel(true);
        }

        failIf(expectedResult, 0);

        return opsPerSecond;
    }

    public static void main(String[] args) throws Exception
    {
        new OneToThreePipelineQueueThroughputTest().testImplementations();
    }
}
