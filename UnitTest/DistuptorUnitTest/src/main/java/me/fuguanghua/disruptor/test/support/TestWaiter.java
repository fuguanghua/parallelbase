package me.fuguanghua.disruptor.test.support;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

public final class TestWaiter implements Callable<List<StubEvent>>
{
    private final long toWaitForSequence;
    private final long initialSequence;
    private final CyclicBarrier cyclicBarrier;
    private final SequenceBarrier sequenceBarrier;
    private final RingBuffer<StubEvent> ringBuffer;

    public TestWaiter(
        final CyclicBarrier cyclicBarrier,
        final SequenceBarrier sequenceBarrier,
        final RingBuffer<StubEvent> ringBuffer,
        final long initialSequence,
        final long toWaitForSequence)
    {
        this.cyclicBarrier = cyclicBarrier;
        this.initialSequence = initialSequence;
        this.ringBuffer = ringBuffer;
        this.toWaitForSequence = toWaitForSequence;
        this.sequenceBarrier = sequenceBarrier;
    }

    @Override
    public List<StubEvent> call() throws Exception
    {
        cyclicBarrier.await();
        sequenceBarrier.waitFor(toWaitForSequence);

        final List<StubEvent> messages = new ArrayList<StubEvent>();
        for (long l = initialSequence; l <= toWaitForSequence; l++)
        {
            messages.add(ringBuffer.get(l));
        }

        return messages;
    }
}