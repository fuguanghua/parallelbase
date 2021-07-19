package me.fuguanghua.perftest.support;

import com.lmax.disruptor.WorkHandler;
import me.fuguanghua.perftest.util.PaddedLong;

public final class EventCountingWorkHandler
    implements WorkHandler<ValueEvent>
{
    private final PaddedLong[] counters;
    private final int index;

    public EventCountingWorkHandler(final PaddedLong[] counters, final int index)
    {
        this.counters = counters;
        this.index = index;
    }

    @Override
    public void onEvent(final ValueEvent event) throws Exception
    {
        counters[index].set(counters[index].get() + 1L);
    }
}
