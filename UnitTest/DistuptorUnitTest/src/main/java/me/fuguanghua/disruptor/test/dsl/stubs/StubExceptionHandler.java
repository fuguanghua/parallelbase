package me.fuguanghua.disruptor.test.dsl.stubs;

import com.lmax.disruptor.ExceptionHandler;

import java.util.concurrent.atomic.AtomicReference;

public class StubExceptionHandler implements ExceptionHandler<Object>
{
    private final AtomicReference<Throwable> exceptionHandled;

    public StubExceptionHandler(final AtomicReference<Throwable> exceptionHandled)
    {
        this.exceptionHandled = exceptionHandled;
    }

    public void handleEventException(final Throwable ex, final long sequence, final Object event)
    {
        exceptionHandled.set(ex);
    }

    @Override
    public void handleOnStartException(final Throwable ex)
    {
        exceptionHandled.set(ex);
    }

    @Override
    public void handleOnShutdownException(final Throwable ex)
    {
        exceptionHandled.set(ex);
    }
}
