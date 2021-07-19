package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.MultiProducerSequencer;
import com.lmax.disruptor.Sequencer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MultiProducerSequencerTest
{
    private final Sequencer publisher = new MultiProducerSequencer(1024, new BlockingWaitStrategy());

    @Test
    public void shouldOnlyAllowMessagesToBeAvailableIfSpecificallyPublished() throws Exception
    {
        publisher.publish(3);
        publisher.publish(5);

        assertThat(publisher.isAvailable(0), is(false));
        assertThat(publisher.isAvailable(1), is(false));
        assertThat(publisher.isAvailable(2), is(false));
        assertThat(publisher.isAvailable(3), is(true));
        assertThat(publisher.isAvailable(4), is(false));
        assertThat(publisher.isAvailable(5), is(true));
        assertThat(publisher.isAvailable(6), is(false));
    }
}
