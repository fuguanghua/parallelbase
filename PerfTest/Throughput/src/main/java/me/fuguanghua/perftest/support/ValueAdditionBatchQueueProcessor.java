package me.fuguanghua.perftest.support;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public final class ValueAdditionBatchQueueProcessor implements Runnable
{
    private volatile boolean running;
    private long value;
    private long sequence;
    private CountDownLatch latch;

    private final BlockingQueue<Long> blockingQueue;
    private final ArrayList<Long> batch = new ArrayList<Long>(100);
    private final long count;

    public ValueAdditionBatchQueueProcessor(final BlockingQueue<Long> blockingQueue, final long count)
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
                long v = blockingQueue.take();
                sequence++;

                this.value += v;

                int c = blockingQueue.drainTo(batch, 100);
                sequence += c;

                v = 0;
                for (int i = 0, n = batch.size(); i < n; i++)
                {
                    v += batch.get(i);
                }

                this.value += v;

                batch.clear();

                if (sequence == count)
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

    @Override
    public String toString()
    {
        return "ValueAdditionBatchQueueProcessor{" +
            "value=" + value +
            ", sequence=" + sequence +
            ", count=" + count +
            '}';
    }
}
