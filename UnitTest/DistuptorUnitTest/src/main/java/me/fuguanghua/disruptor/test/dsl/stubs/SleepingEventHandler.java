package me.fuguanghua.disruptor.test.dsl.stubs;

import com.lmax.disruptor.EventHandler;
import me.fuguanghua.disruptor.test.support.TestEvent;

public class SleepingEventHandler implements EventHandler<TestEvent>
{
    @Override
    public void onEvent(final TestEvent entry, final long sequence, final boolean endOfBatch) throws Exception
    {
        Thread.sleep(1000);
    }
}
