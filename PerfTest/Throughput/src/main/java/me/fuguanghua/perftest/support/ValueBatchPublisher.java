package me.fuguanghua.perftest.support;

import com.lmax.disruptor.RingBuffer;

import java.util.concurrent.CyclicBarrier;

public final class ValueBatchPublisher implements Runnable
{
    private final CyclicBarrier cyclicBarrier;
    private final RingBuffer<ValueEvent> ringBuffer;
    private final long iterations;
    private final int batchSize;

    public ValueBatchPublisher(
        final CyclicBarrier cyclicBarrier,
        final RingBuffer<ValueEvent> ringBuffer,
        final long iterations,
        final int batchSize)
    {
        this.cyclicBarrier = cyclicBarrier;
        this.ringBuffer = ringBuffer;
        this.iterations = iterations;
        this.batchSize = batchSize;
    }

    @Override
    public void run()
    {
        try
        {
            cyclicBarrier.await();

            for (long i = 0; i < iterations; i += batchSize)
            {
                long hi = ringBuffer.next(batchSize);
                long lo = hi - (batchSize - 1);
                for (long l = lo; l <= hi; l++)
                {
                    ValueEvent event = ringBuffer.get(l);
                    event.setValue(l);
                }
                ringBuffer.publish(lo, hi);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
