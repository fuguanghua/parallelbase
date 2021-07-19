package me.fuguanghua.perftest.support;

import com.lmax.disruptor.EventFactory;

public final class FizzBuzzEvent
{
    private boolean fizz = false;
    private boolean buzz = false;
    private long value = 0;

    public long getValue()
    {
        return value;
    }

    public void setValue(final long value)
    {
        fizz = false;
        buzz = false;
        this.value = value;
    }

    public boolean isFizz()
    {
        return fizz;
    }

    public void setFizz(final boolean fizz)
    {
        this.fizz = fizz;
    }

    public boolean isBuzz()
    {
        return buzz;
    }

    public void setBuzz(final boolean buzz)
    {
        this.buzz = buzz;
    }

    public static final EventFactory<FizzBuzzEvent> EVENT_FACTORY = new EventFactory<FizzBuzzEvent>()
    {
        public FizzBuzzEvent newInstance()
        {
            return new FizzBuzzEvent();
        }
    };
}
