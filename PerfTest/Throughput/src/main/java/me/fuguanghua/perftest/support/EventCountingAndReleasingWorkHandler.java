package me.fuguanghua.perftest.support;

import com.lmax.disruptor.EventReleaseAware;
import com.lmax.disruptor.EventReleaser;
import com.lmax.disruptor.WorkHandler;
import me.fuguanghua.perftest.util.PaddedLong;

public final class EventCountingAndReleasingWorkHandler
    implements WorkHandler<ValueEvent>, EventReleaseAware
{
    private final PaddedLong[] counters;
    private final int index;
    private EventReleaser eventReleaser;

    public EventCountingAndReleasingWorkHandler(final PaddedLong[] counters, final int index)
    {
        this.counters = counters;
        this.index = index;
    }

    @Override
    public void onEvent(final ValueEvent event) throws Exception
    {
        eventReleaser.release();
        counters[index].set(counters[index].get() + 1L);
    }

    @Override
    public void setEventReleaser(final EventReleaser eventReleaser)
    {
        this.eventReleaser = eventReleaser;
    }
}
