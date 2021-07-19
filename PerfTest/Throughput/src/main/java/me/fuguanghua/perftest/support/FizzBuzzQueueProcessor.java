package me.fuguanghua.perftest.support;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public final class FizzBuzzQueueProcessor implements Runnable
{
    private final FizzBuzzStep fizzBuzzStep;
    private final BlockingQueue<Long> fizzInputQueue;
    private final BlockingQueue<Long> buzzInputQueue;
    private final BlockingQueue<Boolean> fizzOutputQueue;
    private final BlockingQueue<Boolean> buzzOutputQueue;
    private final long count;

    private volatile boolean running;
    private long fizzBuzzCounter = 0;
    private long sequence;
    private CountDownLatch latch = null;

    public FizzBuzzQueueProcessor(
        final FizzBuzzStep fizzBuzzStep,
        final BlockingQueue<Long> fizzInputQueue,
        final BlockingQueue<Long> buzzInputQueue,
        final BlockingQueue<Boolean> fizzOutputQueue,
        final BlockingQueue<Boolean> buzzOutputQueue, final long count)
    {
        this.fizzBuzzStep = fizzBuzzStep;

        this.fizzInputQueue = fizzInputQueue;
        this.buzzInputQueue = buzzInputQueue;
        this.fizzOutputQueue = fizzOutputQueue;
        this.buzzOutputQueue = buzzOutputQueue;
        this.count = count;
    }

    public long getFizzBuzzCounter()
    {
        return fizzBuzzCounter;
    }

    public void reset(final CountDownLatch latch)
    {
        fizzBuzzCounter = 0L;
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
                switch (fizzBuzzStep)
                {
                    case FIZZ:
                    {
                        Long value = fizzInputQueue.take();
                        fizzOutputQueue.put(Boolean.valueOf(0 == (value.longValue() % 3)));
                        break;
                    }

                    case BUZZ:
                    {
                        Long value = buzzInputQueue.take();
                        buzzOutputQueue.put(Boolean.valueOf(0 == (value.longValue() % 5)));
                        break;
                    }

                    case FIZZ_BUZZ:
                    {
                        final boolean fizz = fizzOutputQueue.take().booleanValue();
                        final boolean buzz = buzzOutputQueue.take().booleanValue();
                        if (fizz && buzz)
                        {
                            ++fizzBuzzCounter;
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
