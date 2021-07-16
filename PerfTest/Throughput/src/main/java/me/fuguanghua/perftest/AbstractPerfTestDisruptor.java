package me.fuguanghua.perftest;


public abstract class AbstractPerfTestDisruptor
{
    public static final int RUNS = 7;

    protected void testImplementations() throws Exception
    {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (getRequiredProcessorCount() > availableProcessors)
        {
            System.out.print("*** Warning ***: your system has insufficient processors to execute the test efficiently. ");
            System.out.println("Processors required = " + getRequiredProcessorCount() + " available = " + availableProcessors);
        }

        long[] disruptorOps = new long[RUNS];

        System.out.println("启动分发器测试");
        for (int i = 0; i < RUNS; i++)
        {
            System.gc();
            disruptorOps[i] = runDisruptorPass();
            System.out.format("Run %d, 分发器=%,d ops/sec%n", i, Long.valueOf(disruptorOps[i]));
        }
    }

    public static void printResults(final String className, final long[] disruptorOps, final long[] queueOps)
    {
        for (int i = 0; i < RUNS; i++)
        {
            System.out.format("%s run %d: 阻塞队列=%,d 分发器=%,d ops/sec\n",
                              className, Integer.valueOf(i), Long.valueOf(queueOps[i]), Long.valueOf(disruptorOps[i]));
        }
    }

    protected abstract int getRequiredProcessorCount();

    protected abstract long runDisruptorPass() throws Exception;
}
