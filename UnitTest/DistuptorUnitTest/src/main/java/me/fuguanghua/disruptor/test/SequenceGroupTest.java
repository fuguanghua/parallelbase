package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceGroup;
import me.fuguanghua.disruptor.test.support.TestEvent;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public final class SequenceGroupTest
{
    @Test
    public void shouldReturnMaxSequenceWhenEmptyGroup()
    {
        final SequenceGroup sequenceGroup = new SequenceGroup();
        assertEquals(Long.MAX_VALUE, sequenceGroup.get());
    }

    @Test
    public void shouldAddOneSequenceToGroup()
    {
        final Sequence sequence = new Sequence(7L);
        final SequenceGroup sequenceGroup = new SequenceGroup();

        sequenceGroup.add(sequence);

        assertEquals(sequence.get(), sequenceGroup.get());
    }

    @Test
    public void shouldNotFailIfTryingToRemoveNotExistingSequence() throws Exception
    {
        SequenceGroup group = new SequenceGroup();
        group.add(new Sequence());
        group.add(new Sequence());
        group.remove(new Sequence());
    }

    @Test
    public void shouldReportTheMinimumSequenceForGroupOfTwo()
    {
        final Sequence sequenceThree = new Sequence(3L);
        final Sequence sequenceSeven = new Sequence(7L);
        final SequenceGroup sequenceGroup = new SequenceGroup();

        sequenceGroup.add(sequenceSeven);
        sequenceGroup.add(sequenceThree);

        assertEquals(sequenceThree.get(), sequenceGroup.get());
    }

    @Test
    public void shouldReportSizeOfGroup()
    {
        final SequenceGroup sequenceGroup = new SequenceGroup();
        sequenceGroup.add(new Sequence());
        sequenceGroup.add(new Sequence());
        sequenceGroup.add(new Sequence());

        assertEquals(3, sequenceGroup.size());
    }

    @Test
    public void shouldRemoveSequenceFromGroup()
    {
        final Sequence sequenceThree = new Sequence(3L);
        final Sequence sequenceSeven = new Sequence(7L);
        final SequenceGroup sequenceGroup = new SequenceGroup();

        sequenceGroup.add(sequenceSeven);
        sequenceGroup.add(sequenceThree);

        assertEquals(sequenceThree.get(), sequenceGroup.get());

        assertTrue(sequenceGroup.remove(sequenceThree));
        assertEquals(sequenceSeven.get(), sequenceGroup.get());
        assertEquals(1, sequenceGroup.size());
    }

    @Test
    public void shouldRemoveSequenceFromGroupWhereItBeenAddedMultipleTimes()
    {
        final Sequence sequenceThree = new Sequence(3L);
        final Sequence sequenceSeven = new Sequence(7L);
        final SequenceGroup sequenceGroup = new SequenceGroup();

        sequenceGroup.add(sequenceThree);
        sequenceGroup.add(sequenceSeven);
        sequenceGroup.add(sequenceThree);

        assertEquals(sequenceThree.get(), sequenceGroup.get());

        assertTrue(sequenceGroup.remove(sequenceThree));
        assertEquals(sequenceSeven.get(), sequenceGroup.get());
        assertEquals(1, sequenceGroup.size());
    }

    @Test
    public void shouldSetGroupSequenceToSameValue()
    {
        final Sequence sequenceThree = new Sequence(3L);
        final Sequence sequenceSeven = new Sequence(7L);
        final SequenceGroup sequenceGroup = new SequenceGroup();

        sequenceGroup.add(sequenceSeven);
        sequenceGroup.add(sequenceThree);

        final long expectedSequence = 11L;
        sequenceGroup.set(expectedSequence);

        assertEquals(expectedSequence, sequenceThree.get());
        assertEquals(expectedSequence, sequenceSeven.get());
    }

    @Test
    public void shouldAddWhileRunning() throws Exception
    {
        RingBuffer<TestEvent> ringBuffer = RingBuffer.createSingleProducer(TestEvent.EVENT_FACTORY, 32);
        final Sequence sequenceThree = new Sequence(3L);
        final Sequence sequenceSeven = new Sequence(7L);
        final SequenceGroup sequenceGroup = new SequenceGroup();
        sequenceGroup.add(sequenceSeven);

        for (int i = 0; i < 11; i++)
        {
            ringBuffer.publish(ringBuffer.next());
        }

        sequenceGroup.addWhileRunning(ringBuffer, sequenceThree);
        assertThat(sequenceThree.get(), is(10L));
    }
}
