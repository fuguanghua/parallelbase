package me.fuguanghua.disruptor.test.util;

/**
 * Holder class for a long value.
 */
public class MutableLong
{
    private long value = 0L;

    /**
     * Default constructor
     */
    public MutableLong()
    {
    }

    /**
     * Construct the holder with initial value.
     *
     * @param initialValue to be initially set.
     */
    public MutableLong(final long initialValue)
    {
        this.value = initialValue;
    }

    /**
     * Get the long value.
     *
     * @return the long value.
     */
    public long get()
    {
        return value;
    }

    /**
     * Set the long value.
     *
     * @param value to set.
     */
    public void set(final long value)
    {
        this.value = value;
    }

    /**
     * Increments the value
     */
    public void increment()
    {
        value++;
    }
}
