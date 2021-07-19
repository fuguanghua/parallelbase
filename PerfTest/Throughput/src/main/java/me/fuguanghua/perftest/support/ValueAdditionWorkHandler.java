package me.fuguanghua.perftest.support;

import com.lmax.disruptor.WorkHandler;

public class ValueAdditionWorkHandler implements WorkHandler<ValueEvent>
{
    private long total;

    @Override
    public void onEvent(ValueEvent event) throws Exception
    {
        long value = event.getValue();
        total += value;
    }

    public long getTotal()
    {
        return total;
    }
}
