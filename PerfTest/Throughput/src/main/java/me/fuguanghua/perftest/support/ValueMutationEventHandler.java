package me.fuguanghua.perftest.support;

import com.lmax.disruptor.EventHandler;
import me.fuguanghua.perftest.util.PaddedLong;

import java.util.concurrent.CountDownLatch;

public final class ValueMutationEventHandler implements EventHandler<ValueEvent>
{
    private final Operation operation;
    private final PaddedLong value = new PaddedLong();
    private long count;
    private CountDownLatch latch;

    public ValueMutationEventHandler(final Operation operation)
    {
        this.operation = operation;
    }

    public long getValue()
    {
        return value.get();
    }

    public void reset(final CountDownLatch latch, final long expectedCount)
    {
        value.set(0L);
        this.latch = latch;
        count = expectedCount;
    }

    @Override
    public void onEvent(final ValueEvent event, final long sequence, final boolean endOfBatch) throws Exception
    {
        value.set(operation.op(value.get(), event.getValue()));

        if (count == sequence)
        {
            latch.countDown();
        }
    }
}
