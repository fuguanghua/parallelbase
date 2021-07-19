package me.fuguanghua.disruptor.test;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.IgnoreExceptionHandler;
import me.fuguanghua.disruptor.test.support.TestEvent;
import org.junit.Test;

public final class IgnoreExceptionHandlerTest
{
    @Test
    public void shouldHandleAndIgnoreException()
    {
        final Exception ex = new Exception();
        final TestEvent event = new TestEvent();

        ExceptionHandler<Object> exceptionHandler = new IgnoreExceptionHandler();
        exceptionHandler.handleEventException(ex, 0L, event);
    }
}
