package me.fuguanghua.perftest;


public abstract class AbstractPerfTestQueue
{
    public static final int RUNS = 7;

    protected void testImplementations()
        throws Exception
    {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (getRequiredProcessorCount() > availableProcessors)
        {
            System.out.print(
                "*** Warning ***: your system has insufficient processors to execute the test efficiently. ");
            System.out.println(
                "Processors required = " + getRequiredProcessorCount() + " available = " + availableProcessors);
        }

        long[] queueOps = new long[RUNS];

        System.out.println("Starting Queue tests");
        for (int i = 0; i < RUNS; i++)
        {
            System.gc();
            queueOps[i] = runQueuePass();
            System.out.format("Run %d, BlockingQueue=%,d ops/sec%n", i, Long.valueOf(queueOps[i]));
        }
    }

    public static void printResults(final String className, final long[] disruptorOps, final long[] queueOps)
    {
        for (int i = 0; i < RUNS; i++)
        {
            System.out.format(
                "%s run %d: BlockingQueue=%,d Disruptor=%,d ops/sec\n",
                className, Integer.valueOf(i), Long.valueOf(queueOps[i]), Long.valueOf(disruptorOps[i]));
        }
    }

    protected abstract int getRequiredProcessorCount();

    protected abstract long runQueuePass() throws Exception;
}
