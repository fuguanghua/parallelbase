package me.fuguanghua.parallelbase.futuremaster.common.test.guava;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.ConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.common.CommonConvertedFutureTestHelper;

import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GuavaConvertedFutureTestHelper extends CommonConvertedFutureTestHelper implements ConvertedFutureTestHelper<ListenableFuture<String>> {
    private final FutureCallback<String> callback = mock(FutureCallback.class);

    @Override
    public void waitForCalculationToFinish(ListenableFuture<String> convertedFuture) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        Futures.addCallback(convertedFuture, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                latch.countDown();
            }
        }, MoreExecutors.directExecutor());

        latch.await(1, TimeUnit.SECONDS);
    }

    @Override
    public void verifyCallbackCalledWithException(Exception exception) {
        waitForCallback();
        verify(callback).onFailure(exception);
    }

    @Override
    public void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) {
        waitForCallback();
        verify(callback).onFailure(any(exceptionClass));
    }

    @Override
    public void verifyCallbackCalledWithCorrectValue() {
        waitForCallback();
        verify(callback).onSuccess(AbstractConverterTest.VALUE);
    }

    @Override
    public void addCallbackTo(ListenableFuture<String> convertedFuture) {
        Executor executor = new ThreadPoolExecutor(1,
                2,
                1000,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        /*
        Executor executor = new ThreadPoolExecutor(1,
        2,
        1000,
        TimeUnit.MILLISECONDS,
        new PriorityBlockingQueue<Runnable>(),
        Executors.defaultThreadFactory(),
        new ThreadPoolExecutor.AbortPolicy());
         */
        Futures.addCallback(convertedFuture, callback, executor);
        convertedFuture.addListener(this::callbackCalled, MoreExecutors.directExecutor());
    }
}
