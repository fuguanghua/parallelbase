package me.fuguanghua.perftest.support;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;

public final class ValueQueuePublisher implements Runnable
{
    private final CyclicBarrier cyclicBarrier;
    private final BlockingQueue<Long> blockingQueue;
    private final long iterations;

    public ValueQueuePublisher(
        final CyclicBarrier cyclicBarrier, final BlockingQueue<Long> blockingQueue, final long iterations)
    {
        this.cyclicBarrier = cyclicBarrier;
        this.blockingQueue = blockingQueue;
        this.iterations = iterations;
    }

    @Override
    public void run()
    {
        try
        {
            cyclicBarrier.await();
            for (long i = 0; i < iterations; i++)
            {
                blockingQueue.put(Long.valueOf(i));
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
