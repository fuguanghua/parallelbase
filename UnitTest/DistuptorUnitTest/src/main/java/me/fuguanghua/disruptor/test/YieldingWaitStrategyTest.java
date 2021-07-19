package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.YieldingWaitStrategy;
import org.junit.Test;

import static me.fuguanghua.disruptor.test.support.WaitStrategyTestUtil.assertWaitForWithDelayOf;

public class YieldingWaitStrategyTest
{

    @Test
    public void shouldWaitForValue() throws Exception
    {
        assertWaitForWithDelayOf(50, new YieldingWaitStrategy());
    }
}
