package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.SleepingWaitStrategy;
import org.junit.Test;

import static me.fuguanghua.disruptor.test.support.WaitStrategyTestUtil.assertWaitForWithDelayOf;

public class SleepingWaitStrategyTest
{
    @Test
    public void shouldWaitForValue() throws Exception
    {
        assertWaitForWithDelayOf(50, new SleepingWaitStrategy());
    }
}
