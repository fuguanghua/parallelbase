package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.*;
import me.fuguanghua.disruptor.test.support.StubEvent;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static com.lmax.disruptor.RingBuffer.createMultiProducer;
import static org.junit.Assert.assertEquals;

public class SequenceReportingCallbackTest
{
    private final CountDownLatch callbackLatch = new CountDownLatch(1);
    private final CountDownLatch onEndOfBatchLatch = new CountDownLatch(1);

    @Test
    public void shouldReportProgressByUpdatingSequenceViaCallback()
        throws Exception
    {
        final RingBuffer<StubEvent> ringBuffer = createMultiProducer(StubEvent.EVENT_FACTORY, 16);
        final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
        final SequenceReportingEventHandler<StubEvent> handler = new TestSequenceReportingEventHandler();
        final BatchEventProcessor<StubEvent> batchEventProcessor = new BatchEventProcessor<StubEvent>(
            ringBuffer, sequenceBarrier, handler);
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());

        Thread thread = new Thread(batchEventProcessor);
        thread.setDaemon(true);
        thread.start();

        assertEquals(-1L, batchEventProcessor.getSequence().get());
        ringBuffer.publish(ringBuffer.next());

        callbackLatch.await();
        assertEquals(0L, batchEventProcessor.getSequence().get());

        onEndOfBatchLatch.countDown();
        assertEquals(0L, batchEventProcessor.getSequence().get());

        batchEventProcessor.halt();
        thread.join();
    }

    private class TestSequenceReportingEventHandler implements SequenceReportingEventHandler<StubEvent>
    {
        private Sequence sequenceCallback;

        @Override
        public void setSequenceCallback(final Sequence sequenceTrackerCallback)
        {
            this.sequenceCallback = sequenceTrackerCallback;
        }

        @Override
        public void onEvent(final StubEvent event, final long sequence, final boolean endOfBatch) throws Exception
        {
            sequenceCallback.set(sequence);
            callbackLatch.countDown();

            if (endOfBatch)
            {
                onEndOfBatchLatch.await();
            }
        }
    }
}
