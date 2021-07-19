package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.AggregateEventHandler;
import me.fuguanghua.disruptor.test.support.DummyEventHandler;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("unchecked")
public final class AggregateEventHandlerTest
{
    private final DummyEventHandler<int[]> eh1 = new DummyEventHandler<int[]>();
    private final DummyEventHandler<int[]> eh2 = new DummyEventHandler<int[]>();
    private final DummyEventHandler<int[]> eh3 = new DummyEventHandler<int[]>();

    @Test
    public void shouldCallOnEventInSequence()
        throws Exception
    {
        final int[] event = {7};
        final long sequence = 3L;
        final boolean endOfBatch = true;

        final AggregateEventHandler<int[]> aggregateEventHandler = new AggregateEventHandler<int[]>(eh1, eh2, eh3);

        aggregateEventHandler.onEvent(event, sequence, endOfBatch);
        assertLastEvent(event, sequence, eh1, eh2, eh3);
    }

    @Test
    public void shouldCallOnStartInSequence()
        throws Exception
    {
        final AggregateEventHandler<int[]> aggregateEventHandler = new AggregateEventHandler<int[]>(eh1, eh2, eh3);

        aggregateEventHandler.onStart();

        assertStartCalls(1, eh1, eh2, eh3);
    }

    @Test
    public void shouldCallOnShutdownInSequence()
        throws Exception
    {
        final AggregateEventHandler<int[]> aggregateEventHandler = new AggregateEventHandler<int[]>(eh1, eh2, eh3);

        aggregateEventHandler.onShutdown();

        assertShutoownCalls(1, eh1, eh2, eh3);
    }

    @Test
    public void shouldHandleEmptyListOfEventHandlers() throws Exception
    {
        final AggregateEventHandler<int[]> aggregateEventHandler = new AggregateEventHandler<int[]>();

        aggregateEventHandler.onEvent(new int[]{7}, 0L, true);
        aggregateEventHandler.onStart();
        aggregateEventHandler.onShutdown();
    }

    private static void assertLastEvent(int[] event, long sequence, DummyEventHandler<int[]>... eh1)
    {
        for (DummyEventHandler<int[]> eh : eh1)
        {
            assertThat(eh.lastEvent, is(event));
            assertThat(eh.lastSequence, is(sequence));
        }
    }

    private static void assertStartCalls(int startCalls, DummyEventHandler<int[]>... handlers)
    {
        for (DummyEventHandler<int[]> handler : handlers)
        {
            assertThat(handler.startCalls, is(startCalls));
        }
    }

    private static void assertShutoownCalls(int startCalls, DummyEventHandler<int[]>... handlers)
    {
        for (DummyEventHandler<int[]> handler : handlers)
        {
            assertThat(handler.shutdownCalls, is(startCalls));
        }
    }
}
