package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.*;
import me.fuguanghua.disruptor.test.support.StubEvent;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static com.lmax.disruptor.RingBuffer.createMultiProducer;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class LifecycleAwareTest
{
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);


    private final RingBuffer<StubEvent> ringBuffer = createMultiProducer(StubEvent.EVENT_FACTORY, 16);
    private final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
    private final LifecycleAwareEventHandler handler = new LifecycleAwareEventHandler();
    private final BatchEventProcessor<StubEvent> batchEventProcessor =
        new BatchEventProcessor<StubEvent>(ringBuffer, sequenceBarrier, handler);

    @Test
    public void shouldNotifyOfBatchProcessorLifecycle() throws Exception
    {
        new Thread(batchEventProcessor).start();

        startLatch.await();
        batchEventProcessor.halt();

        shutdownLatch.await();

        assertThat(Integer.valueOf(handler.startCounter), is(Integer.valueOf(1)));
        assertThat(Integer.valueOf(handler.shutdownCounter), is(Integer.valueOf(1)));
    }

    private final class LifecycleAwareEventHandler implements EventHandler<StubEvent>, LifecycleAware
    {
        private int startCounter = 0;
        private int shutdownCounter = 0;

        @Override
        public void onEvent(final StubEvent event, final long sequence, final boolean endOfBatch) throws Exception
        {
        }

        @Override
        public void onStart()
        {
            ++startCounter;
            startLatch.countDown();
        }

        @Override
        public void onShutdown()
        {
            ++shutdownCounter;
            shutdownLatch.countDown();
        }
    }
}
