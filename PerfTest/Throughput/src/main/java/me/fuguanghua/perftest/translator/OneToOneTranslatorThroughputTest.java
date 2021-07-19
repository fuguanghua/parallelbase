package me.fuguanghua.perftest.translator;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestDisruptor;
import me.fuguanghua.perftest.support.PerfTestUtil;
import me.fuguanghua.perftest.support.ValueAdditionEventHandler;
import me.fuguanghua.perftest.support.ValueEvent;
import me.fuguanghua.perftest.util.MutableLong;


import java.util.concurrent.CountDownLatch;

import static me.fuguanghua.perftest.support.PerfTestUtil.failIfNot;

/**
 * <pre>
 * UniCast a series of items between 1 publisher and 1 event processor using the EventTranslator API
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
public final class OneToOneTranslatorThroughputTest extends AbstractPerfTestDisruptor
{
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 100L;
    private final long expectedResult = PerfTestUtil.accumulatedAddition(ITERATIONS);
    private final ValueAdditionEventHandler handler = new ValueAdditionEventHandler();
    private final RingBuffer<ValueEvent> ringBuffer;
    private final MutableLong value = new MutableLong(0);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public OneToOneTranslatorThroughputTest()
    {
        Disruptor<ValueEvent> disruptor =
            new Disruptor<ValueEvent>(
                ValueEvent.EVENT_FACTORY,
                BUFFER_SIZE, DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new YieldingWaitStrategy());
        disruptor.handleEventsWith(handler);
        this.ringBuffer = disruptor.start();
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
        MutableLong value = this.value;

        final CountDownLatch latch = new CountDownLatch(1);
        long expectedCount = ringBuffer.getMinimumGatingSequence() + ITERATIONS;

        handler.reset(latch, expectedCount);
        long start = System.currentTimeMillis();

        final RingBuffer<ValueEvent> rb = ringBuffer;

        for (long l = 0; l < ITERATIONS; l++)
        {
            value.set(l);
            rb.publishEvent(Translator.INSTANCE, value);
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);
        waitForEventProcessorSequence(expectedCount);

        failIfNot(expectedResult, handler.getValue());

        return opsPerSecond;
    }

    private static class Translator implements EventTranslatorOneArg<ValueEvent, MutableLong>
    {
        private static final Translator INSTANCE = new Translator();

        @Override
        public void translateTo(ValueEvent event, long sequence, MutableLong arg0)
        {
            event.setValue(arg0.get());
        }
    }

    private void waitForEventProcessorSequence(long expectedCount) throws InterruptedException
    {
        while (ringBuffer.getMinimumGatingSequence() != expectedCount)
        {
            Thread.sleep(1);
        }
    }

    public static void main(String[] args) throws Exception
    {
        OneToOneTranslatorThroughputTest test = new OneToOneTranslatorThroughputTest();
        test.testImplementations();
    }
}
