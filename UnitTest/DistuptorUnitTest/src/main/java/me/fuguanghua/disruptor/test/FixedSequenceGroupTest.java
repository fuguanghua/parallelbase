package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.FixedSequenceGroup;
import com.lmax.disruptor.Sequence;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class FixedSequenceGroupTest
{

    @Test
    public void shouldReturnMinimumOf2Sequences() throws Exception
    {
        Sequence sequence1 = new Sequence(34);
        Sequence sequnece2 = new Sequence(47);
        Sequence group = new FixedSequenceGroup(new Sequence[]{sequence1, sequnece2});

        assertThat(group.get(), is(34L));
        sequence1.set(35);
        assertThat(group.get(), is(35L));
        sequence1.set(48);
        assertThat(group.get(), is(47L));
    }
}
