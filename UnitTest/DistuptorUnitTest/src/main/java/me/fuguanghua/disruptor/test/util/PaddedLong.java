package me.fuguanghua.disruptor.test.util;

/**
 * Cache line padded long variable to be used when false sharing maybe an issue.
 */
public final class PaddedLong extends MutableLong
{
    public volatile long p1, p2, p3, p4, p5, p6 = 7L;

    /**
     * Default constructor
     */
    public PaddedLong()
    {
    }

    /**
     * Construct with an initial value.
     *
     * @param initialValue for construction
     */
    public PaddedLong(final long initialValue)
    {
        super(initialValue);
    }

    public long sumPaddingToPreventOptimisation()
    {
        return p1 + p2 + p3 + p4 + p5 + p6;
    }
}
