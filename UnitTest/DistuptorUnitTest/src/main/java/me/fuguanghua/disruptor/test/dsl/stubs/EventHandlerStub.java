package me.fuguanghua.disruptor.test.dsl.stubs;

import com.lmax.disruptor.EventHandler;

import java.util.concurrent.CountDownLatch;

public class EventHandlerStub<T> implements EventHandler<T>
{
    private final CountDownLatch countDownLatch;

    public EventHandlerStub(final CountDownLatch countDownLatch)
    {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onEvent(final T entry, final long sequence, final boolean endOfBatch) throws Exception
    {
        countDownLatch.countDown();
    }
}
