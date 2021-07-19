package me.fuguanghua.perftest.support;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public final class ValueAdditionQueueProcessor implements Runnable
{
    private volatile boolean running;
    private long value;
    private long sequence;
    private CountDownLatch latch;

    private final BlockingQueue<Long> blockingQueue;
    private final long count;

    public ValueAdditionQueueProcessor(final BlockingQueue<Long> blockingQueue, final long count)
    {
        this.blockingQueue = blockingQueue;
        this.count = count;
    }

    public long getValue()
    {
        return value;
    }

    public void reset(final CountDownLatch latch)
    {
        value = 0L;
        sequence = 0L;
        this.latch = latch;
    }

    public void halt()
    {
        running = false;
    }

    @Override
    public void run()
    {
        running = true;
        while (true)
        {
            try
            {
                long value = blockingQueue.take().longValue();
                this.value += value;

                if (sequence++ == count)
                {
                    latch.countDown();
                }
            }
            catch (InterruptedException ex)
            {
                if (!running)
                {
                    break;
                }
            }
        }
    }
}
