package me.fuguanghua.disruptor.test.dsl.stubs;

import com.lmax.disruptor.RingBuffer;
import me.fuguanghua.disruptor.test.support.TestEvent;

public class StubPublisher implements Runnable
{
    private volatile boolean running = true;
    private volatile int publicationCount = 0;

    private final RingBuffer<TestEvent> ringBuffer;

    public StubPublisher(final RingBuffer<TestEvent> ringBuffer)
    {
        this.ringBuffer = ringBuffer;
    }

    public void run()
    {
        while (running)
        {
            final long sequence = ringBuffer.next();
            //final TestEvent entry = ringBuffer.get(sequence);
            ringBuffer.publish(sequence);
            publicationCount++;
        }
    }

    public int getPublicationCount()
    {
        return publicationCount;
    }

    public void halt()
    {
        running = false;
    }
}
