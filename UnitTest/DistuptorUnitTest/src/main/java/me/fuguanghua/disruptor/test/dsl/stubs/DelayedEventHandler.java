package me.fuguanghua.disruptor.test.dsl.stubs;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import me.fuguanghua.disruptor.test.support.TestEvent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class DelayedEventHandler implements EventHandler<TestEvent>, LifecycleAware
{
    private final AtomicBoolean readyToProcessEvent = new AtomicBoolean(false);
    private volatile boolean stopped = false;
    private final CyclicBarrier barrier;

    public DelayedEventHandler(CyclicBarrier barrier)
    {
        this.barrier = barrier;
    }

    public DelayedEventHandler()
    {
        this(new CyclicBarrier(2));
    }

    @Override
    public void onEvent(final TestEvent entry, final long sequence, final boolean endOfBatch) throws Exception
    {
        waitForAndSetFlag(false);
    }

    public void processEvent()
    {
        waitForAndSetFlag(true);
    }

    public void stopWaiting()
    {
        stopped = true;
    }

    private void waitForAndSetFlag(final boolean newValue)
    {
        while (!stopped && !Thread.currentThread().isInterrupted() &&
            !readyToProcessEvent.compareAndSet(!newValue, newValue))
        {
            Thread.yield();
        }
    }

    @Override
    public void onStart()
    {
        try
        {
            barrier.await();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (BrokenBarrierException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onShutdown()
    {
    }

    public void awaitStart() throws InterruptedException, BrokenBarrierException
    {
        barrier.await();
    }
}
