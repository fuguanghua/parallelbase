package me.fuguanghua.perftest.support;

import com.lmax.disruptor.RingBuffer;

import java.util.concurrent.CyclicBarrier;

public final class ValuePublisher implements Runnable
{
    private final CyclicBarrier cyclicBarrier;
    private final RingBuffer<ValueEvent> ringBuffer;
    private final long iterations;

    public ValuePublisher(
        final CyclicBarrier cyclicBarrier, final RingBuffer<ValueEvent> ringBuffer, final long iterations)
    {
        this.cyclicBarrier = cyclicBarrier;
        this.ringBuffer = ringBuffer;
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
                long sequence = ringBuffer.next();
                ValueEvent event = ringBuffer.get(sequence);
                event.setValue(i);
                ringBuffer.publish(sequence);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
