package me.fuguanghua.perftest.support;

import me.fuguanghua.perftest.util.PaddedLong;

import java.util.concurrent.BlockingQueue;

public final class EventCountingQueueProcessor implements Runnable
{
    private volatile boolean running;
    private final BlockingQueue<Long> blockingQueue;
    private final PaddedLong[] counters;
    private final int index;

    public EventCountingQueueProcessor(
        final BlockingQueue<Long> blockingQueue, final PaddedLong[] counters, final int index)
    {
        this.blockingQueue = blockingQueue;
        this.counters = counters;
        this.index = index;
    }

    public void halt()
    {
        running = false;
    }

    @Override
    public void run()
    {
        running = true;
        while (running)
        {
            try
            {
                blockingQueue.take();
                counters[index].set(counters[index].get() + 1L);
            }
            catch (InterruptedException ex)
            {
                break;
            }
        }
    }
}
