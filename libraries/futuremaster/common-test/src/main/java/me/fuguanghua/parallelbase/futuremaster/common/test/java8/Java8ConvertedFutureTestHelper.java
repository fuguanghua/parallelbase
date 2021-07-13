package me.fuguanghua.parallelbase.futuremaster.common.test.java8;

import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.ConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.common.CommonConvertedFutureTestHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Java8ConvertedFutureTestHelper extends CommonConvertedFutureTestHelper implements ConvertedFutureTestHelper<CompletableFuture<String>> {

    private final Consumer<String> callback = mock(Consumer.class);

    private final Function<Throwable, String> exceptionHandler = mock(Function.class);

    @Override
    public void addCallbackTo(CompletableFuture<String> convertedFuture) {
        convertedFuture.exceptionally(exceptionHandler).thenAccept(callback).thenRun(this::callbackCalled);
    }

    @Override
    public void verifyCallbackCalledWithCorrectValue() throws InterruptedException {
        waitForCallback();
        verify(callback).accept(AbstractConverterTest.VALUE);
    }

    @Override
    public void waitForCalculationToFinish(CompletableFuture<String> convertedFuture) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        convertedFuture.thenRun(latch::countDown);
        latch.await(1, TimeUnit.SECONDS);
    }

    @Override
    public void verifyCallbackCalledWithException(Exception exception) {
        waitForCallback();
        verify(exceptionHandler).apply(exception);

    }

    @Override
    public void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) {
        waitForCallback();
        verify(exceptionHandler).apply(any(exceptionClass));
    }

}
