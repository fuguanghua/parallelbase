package me.fuguanghua.perftest.support;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public final class FunctionQueueProcessor implements Runnable
{
    private final FunctionStep functionStep;
    private final BlockingQueue<long[]> stepOneQueue;
    private final BlockingQueue<Long> stepTwoQueue;
    private final BlockingQueue<Long> stepThreeQueue;
    private final long count;

    private volatile boolean running;
    private long stepThreeCounter;
    private long sequence;
    private CountDownLatch latch;

    public FunctionQueueProcessor(
        final FunctionStep functionStep,
        final BlockingQueue<long[]> stepOneQueue,
        final BlockingQueue<Long> stepTwoQueue,
        final BlockingQueue<Long> stepThreeQueue,
        final long count)
    {
        this.functionStep = functionStep;
        this.stepOneQueue = stepOneQueue;
        this.stepTwoQueue = stepTwoQueue;
        this.stepThreeQueue = stepThreeQueue;
        this.count = count;
    }

    public long getStepThreeCounter()
    {
        return stepThreeCounter;
    }

    public void reset(final CountDownLatch latch)
    {
        stepThreeCounter = 0L;
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
                switch (functionStep)
                {
                    case ONE:
                    {
                        long[] values = stepOneQueue.take();
                        stepTwoQueue.put(Long.valueOf(values[0] + values[1]));
                        break;
                    }

                    case TWO:
                    {
                        Long value = stepTwoQueue.take();
                        stepThreeQueue.put(Long.valueOf(value.longValue() + 3));
                        break;
                    }

                    case THREE:
                    {
                        Long value = stepThreeQueue.take();
                        long testValue = value.longValue();
                        if ((testValue & 4L) == 4L)
                        {
                            ++stepThreeCounter;
                        }
                        break;
                    }
                }

                if (null != latch && sequence++ == count)
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
