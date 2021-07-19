package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.FatalExceptionHandler;
import me.fuguanghua.disruptor.test.support.TestEvent;
import org.junit.Assert;
import org.junit.Test;

public final class FatalExceptionHandlerTest
{
    @Test
    public void shouldHandleFatalException()
    {
        final Exception causeException = new Exception();
        final TestEvent event = new TestEvent();

        ExceptionHandler<Object> exceptionHandler = new FatalExceptionHandler();

        try
        {
            exceptionHandler.handleEventException(causeException, 0L, event);
        }
        catch (RuntimeException ex)
        {
            Assert.assertEquals(causeException, ex.getCause());
        }
    }
}
