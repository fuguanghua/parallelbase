package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.NoOpEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import me.fuguanghua.disruptor.test.support.LongEvent;
import org.junit.Test;

import static com.lmax.disruptor.RingBuffer.createMultiProducer;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EventPublisherTest implements EventTranslator<LongEvent>
{
    private static final int BUFFER_SIZE = 32;
    private RingBuffer<LongEvent> ringBuffer = createMultiProducer(LongEvent.FACTORY, BUFFER_SIZE);

    @Test
    public void shouldPublishEvent()
    {
        ringBuffer.addGatingSequences(new NoOpEventProcessor(ringBuffer).getSequence());

        ringBuffer.publishEvent(this);
        ringBuffer.publishEvent(this);

        assertThat(Long.valueOf(ringBuffer.get(0).get()), is(Long.valueOf(0 + 29L)));
        assertThat(Long.valueOf(ringBuffer.get(1).get()), is(Long.valueOf(1 + 29L)));
    }

    @Test
    public void shouldTryPublishEvent() throws Exception
    {
        ringBuffer.addGatingSequences(new Sequence());

        for (int i = 0; i < BUFFER_SIZE; i++)
        {
            assertThat(ringBuffer.tryPublishEvent(this), is(true));
        }

        for (int i = 0; i < BUFFER_SIZE; i++)
        {
            assertThat(Long.valueOf(ringBuffer.get(i).get()), is(Long.valueOf(i + 29L)));
        }

        assertThat(ringBuffer.tryPublishEvent(this), is(false));
    }

    @Override
    public void translateTo(LongEvent event, long sequence)
    {
        event.set(sequence + 29);
    }
}
