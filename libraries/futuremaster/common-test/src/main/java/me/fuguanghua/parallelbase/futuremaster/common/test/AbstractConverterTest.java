package me.fuguanghua.parallelbase.futuremaster.common.test;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * Abstract test conversion from F to type T.
 *
 * @param <F> from
 * @param <T> to
 */
public abstract class AbstractConverterTest<F extends Future<String>, T extends Future<String>> {

    public static final String VALUE = "test";

    protected abstract T convert(F originalFuture);

    protected abstract F convertBack(T converted);

    protected abstract F createFinishedOriginal();

    protected abstract F createExceptionalFuture(Exception exception);

    protected abstract F createRunningFuture();

    protected abstract void addCallbackTo(T convertedFuture);

    protected abstract void verifyCallbackCalledWithCorrectValue() throws InterruptedException;

    protected abstract void waitForCalculationToFinish(T convertedFuture) throws InterruptedException;

    protected abstract void verifyCallbackCalledWithException(Exception exception) throws InterruptedException;

    protected abstract void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) throws InterruptedException;

    protected abstract void finishOriginalFuture();


    @Test
    public void testConvertCompleted() throws ExecutionException, InterruptedException {
        F originalFuture = createFinishedOriginal();

        T converted = convert(originalFuture);
        assertEquals(VALUE, converted.get());
        assertEquals(true, converted.isDone());
        assertEquals(false, converted.isCancelled());
        addCallbackTo(converted);
        verifyCallbackCalledWithCorrectValue();
    }

    @Test
    public void testConvertRunning() throws ExecutionException, InterruptedException {
        F originalFuture = createRunningFuture();
        T convertedFuture = convert(originalFuture);
        addCallbackTo(convertedFuture);
        assertEquals(false, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());
        finishOriginalFuture();

        //wait for the result
        assertEquals(VALUE, convertedFuture.get());
        assertEquals(true, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());

        waitForCalculationToFinish(convertedFuture);
        verifyCallbackCalledWithCorrectValue();
    }


    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        F originalFuture = createRunningFuture();
        originalFuture.cancel(true);

        T convertedFuture = convert(originalFuture);

        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, convertedFuture.isDone());
        assertEquals(true, convertedFuture.isCancelled());
        addCallbackTo(convertedFuture);
        verifyCallbackCalledWithException(CancellationException.class);
    }

    @Test
    public void testCancelNew() throws ExecutionException, InterruptedException {
        F originalFuture = createRunningFuture();
        T convertedFuture = convert(originalFuture);
        convertedFuture.cancel(true);

        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, convertedFuture.isDone());
        assertEquals(true, convertedFuture.isCancelled());
        assertEquals(true, originalFuture.isDone());
        assertEquals(true, originalFuture.isCancelled());
        addCallbackTo(convertedFuture);
        verifyCallbackCalledWithException(CancellationException.class);
    }

    @Test
    public void testCancelBeforeConversion() throws ExecutionException, InterruptedException {
        F originalFuture = createRunningFuture();
        originalFuture.cancel(true);

        T convertedFuture = convert(originalFuture);
        assertFalse(convertedFuture.cancel(true));

        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, originalFuture.isDone());
        assertEquals(true, originalFuture.isCancelled());
        assertEquals(true, convertedFuture.isDone());
        assertEquals(true, convertedFuture.isCancelled());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionOnNull() {
        convert(null);
    }

    @Test
    public void shouldConvertBackToTheSameInstance() {
        F originalFuture = createFinishedOriginal();
        T converted = convert(originalFuture);
        assertSame(originalFuture, convertBack(converted));
    }

    @Test
    public void testCancelCompleted() throws ExecutionException, InterruptedException {
        F originalFuture = createFinishedOriginal();

        T convertedFuture = convert(originalFuture);
        addCallbackTo(convertedFuture);
        waitForCalculationToFinish(convertedFuture);

        assertEquals(VALUE, convertedFuture.get());
        assertEquals(true, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());

        verifyCallbackCalledWithCorrectValue();
        assertFalse(convertedFuture.cancel(true));
    }


    @Test
    public void testConvertWithException() throws ExecutionException, InterruptedException {
        Exception exception = new RuntimeException("test");
        doTestException(exception);
    }

    @Test
    public void testConvertWithIOException() throws ExecutionException, InterruptedException {
        Exception exception = new IOException("test");
        doTestException(exception);
    }

    protected void doTestException(final Exception exception) throws InterruptedException {
        F originalFuture = createExceptionalFuture(exception);
        T convertedFuture = convert(originalFuture);
        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (ExecutionException e) {
            assertEquals(exception, e.getCause());
        }
        assertEquals(true, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());

        addCallbackTo(convertedFuture);
        verifyCallbackCalledWithException(exception);
    }
}
