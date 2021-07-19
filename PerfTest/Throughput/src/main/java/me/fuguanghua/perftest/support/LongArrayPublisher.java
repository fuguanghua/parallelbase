package me.fuguanghua.perftest.support;

import com.lmax.disruptor.RingBuffer;

import java.util.concurrent.CyclicBarrier;

public final class LongArrayPublisher implements Runnable
{
    private final CyclicBarrier cyclicBarrier;
    private final RingBuffer<long[]> ringBuffer;
    private final long iterations;
    private final long arraySize;

    public LongArrayPublisher(
        final CyclicBarrier cyclicBarrier,
        final RingBuffer<long[]> ringBuffer,
        final long iterations,
        final long arraySize)
    {
        this.cyclicBarrier = cyclicBarrier;
        this.ringBuffer = ringBuffer;
        this.iterations = iterations;
        this.arraySize = arraySize;
    }

    @Override
    public void run()
    {
        try
        {
            cyclicBarrier.await();

            for (long i = 0; i < iterations; i++)
            {
                long sequence = ringBuffer.next();
                long[] event = ringBuffer.get(sequence);
                for (int j = 0; j < arraySize; j++)
                {
                    event[j] = i + j;
                }
                ringBuffer.publish(sequence);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
