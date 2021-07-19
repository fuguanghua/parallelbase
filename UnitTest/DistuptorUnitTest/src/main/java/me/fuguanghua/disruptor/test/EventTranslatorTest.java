package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.EventTranslator;
import me.fuguanghua.disruptor.test.support.StubEvent;
import org.junit.Assert;
import org.junit.Test;

public final class EventTranslatorTest
{
    private static final String TEST_VALUE = "Wibble";

    @Test
    public void shouldTranslateOtherDataIntoAnEvent()
    {
        StubEvent event = StubEvent.EVENT_FACTORY.newInstance();
        EventTranslator<StubEvent> eventTranslator = new ExampleEventTranslator(TEST_VALUE);

        eventTranslator.translateTo(event, 0);

        Assert.assertEquals(TEST_VALUE, event.getTestString());
    }

    public static final class ExampleEventTranslator
        implements EventTranslator<StubEvent>
    {
        private final String testValue;

        public ExampleEventTranslator(final String testValue)
        {
            this.testValue = testValue;
        }

        @Override
        public void translateTo(final StubEvent event, long sequence)
        {
            event.setTestString(testValue);
        }
    }
}
