package me.fuguanghua.perftest.workhandler;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.DaemonThreadFactory;
import me.fuguanghua.perftest.AbstractPerfTestDisruptor;
import me.fuguanghua.perftest.support.EventCountingAndReleasingWorkHandler;
import me.fuguanghua.perftest.support.ValueEvent;
import me.fuguanghua.perftest.util.PaddedLong;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.fuguanghua.perftest.support.PerfTestUtil.failIfNot;

public final class OneToThreeReleasingWorkerPoolThroughputTest
    extends AbstractPerfTestDisruptor
{
    private static final int NUM_WORKERS = 3;
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final long ITERATIONS = 1000L * 1000 * 10L;
    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_WORKERS, DaemonThreadFactory.INSTANCE);

    private final PaddedLong[] counters = new PaddedLong[NUM_WORKERS];

    {
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            counters[i] = new PaddedLong();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final EventCountingAndReleasingWorkHandler[] handlers =
        new EventCountingAndReleasingWorkHandler[NUM_WORKERS];

    {
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            handlers[i] = new EventCountingAndReleasingWorkHandler(counters, i);
        }
    }

    private final RingBuffer<ValueEvent> ringBuffer =
        RingBuffer.createSingleProducer(
            ValueEvent.EVENT_FACTORY,
            BUFFER_SIZE,
            new YieldingWaitStrategy());

    private final WorkerPool<ValueEvent> workerPool =
        new WorkerPool<ValueEvent>(
            ringBuffer,
            ringBuffer.newBarrier(),
            new FatalExceptionHandler(),
            handlers);

    {
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount()
    {
        return 4;
    }

    @Override
    protected long runDisruptorPass() throws InterruptedException
    {

        resetCounters();
        RingBuffer<ValueEvent> ringBuffer = workerPool.start(executor);
        long start = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++)
        {
            long sequence = ringBuffer.next();
            ringBuffer.get(sequence).setValue(i);
            ringBuffer.publish(sequence);
        }

        workerPool.drainAndHalt();

        // Workaround to ensure that the last worker(s) have completed after releasing their events
        Thread.sleep(1L);

        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);

        failIfNot(ITERATIONS, sumCounters());

        return opsPerSecond;
    }

    private void resetCounters()
    {
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            counters[i].set(0L);
        }
    }

    private long sumCounters()
    {
        long sumJobs = 0L;
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            sumJobs += counters[i].get();
        }

        return sumJobs;
    }

    public static void main(String[] args) throws Exception
    {
        new OneToThreeReleasingWorkerPoolThroughputTest().testImplementations();
    }
}
