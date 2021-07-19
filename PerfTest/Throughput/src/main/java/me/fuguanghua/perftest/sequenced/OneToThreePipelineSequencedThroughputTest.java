package me.fuguanghua.perftest.sequenced;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestDisruptor;
import me.fuguanghua.perftest.support.FunctionEvent;
import me.fuguanghua.perftest.support.FunctionEventHandler;
import me.fuguanghua.perftest.support.FunctionStep;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;
import static me.fuguanghua.perftest.support.PerfTestUtil.failIfNot;

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
 * Disruptor:
 * ==========
 *                           track to prevent wrap
 *              +----------------------------------------------------------------+
 *              |                                                                |
 *              |                                                                v
 * +----+    +====+    +=====+    +-----+    +=====+    +-----+    +=====+    +-----+
 * | P1 |--->| RB |    | SB1 |<---| EP1 |<---| SB2 |<---| EP2 |<---| SB3 |<---| EP3 |
 * +----+    +====+    +=====+    +-----+    +=====+    +-----+    +=====+    +-----+
 *      claim   ^  get    |   waitFor           |   waitFor           |  waitFor
 *              |         |                     |                     |
 *              +---------+---------------------+---------------------+
 *        </pre>
 *
 * P1  - Publisher 1
 * RB  - RingBuffer
 * SB1 - SequenceBarrier 1
 * EP1 - EventProcessor 1
 * SB2 - SequenceBarrier 2
 * EP2 - EventProcessor 2
 * SB3 - SequenceBarrier 3
 * EP3 - EventProcessor 3
 *
 * </pre>
 */
public final class OneToThreePipelineSequencedThroughputTest extends AbstractPerfTestDisruptor
{
    private static final int NUM_EVENT_PROCESSORS = 3;
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final long ITERATIONS = 1000L * 1000L * 100L;
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

    private final RingBuffer<FunctionEvent> ringBuffer =
        createSingleProducer(FunctionEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());

    private final SequenceBarrier stepOneSequenceBarrier = ringBuffer.newBarrier();
    private final FunctionEventHandler stepOneFunctionHandler = new FunctionEventHandler(FunctionStep.ONE);
    private final BatchEventProcessor<FunctionEvent> stepOneBatchProcessor =
        new BatchEventProcessor<FunctionEvent>(ringBuffer, stepOneSequenceBarrier, stepOneFunctionHandler);

    private final SequenceBarrier stepTwoSequenceBarrier = ringBuffer.newBarrier(stepOneBatchProcessor.getSequence());
    private final FunctionEventHandler stepTwoFunctionHandler = new FunctionEventHandler(FunctionStep.TWO);
    private final BatchEventProcessor<FunctionEvent> stepTwoBatchProcessor =
        new BatchEventProcessor<FunctionEvent>(ringBuffer, stepTwoSequenceBarrier, stepTwoFunctionHandler);

    private final SequenceBarrier stepThreeSequenceBarrier = ringBuffer.newBarrier(stepTwoBatchProcessor.getSequence());
    private final FunctionEventHandler stepThreeFunctionHandler = new FunctionEventHandler(FunctionStep.THREE);
    private final BatchEventProcessor<FunctionEvent> stepThreeBatchProcessor =
        new BatchEventProcessor<FunctionEvent>(ringBuffer, stepThreeSequenceBarrier, stepThreeFunctionHandler);

    {
        ringBuffer.addGatingSequences(stepThreeBatchProcessor.getSequence());
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
        CountDownLatch latch = new CountDownLatch(1);
        stepThreeFunctionHandler.reset(latch, stepThreeBatchProcessor.getSequence().get() + ITERATIONS);

        executor.submit(stepOneBatchProcessor);
        executor.submit(stepTwoBatchProcessor);
        executor.submit(stepThreeBatchProcessor);

        long start = System.currentTimeMillis();

        long operandTwo = OPERAND_TWO_INITIAL_VALUE;
        for (long i = 0; i < ITERATIONS; i++)
        {
            long sequence = ringBuffer.next();
            FunctionEvent event = ringBuffer.get(sequence);
            event.setOperandOne(i);
            event.setOperandTwo(operandTwo--);
            ringBuffer.publish(sequence);
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);

        stepOneBatchProcessor.halt();
        stepTwoBatchProcessor.halt();
        stepThreeBatchProcessor.halt();

        failIfNot(expectedResult, stepThreeFunctionHandler.getStepThreeCounter());

        return opsPerSecond;
    }

    public static void main(String[] args) throws Exception
    {
        new OneToThreePipelineSequencedThroughputTest().testImplementations();
    }
}