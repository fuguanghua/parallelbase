package me.fuguanghua.parallelbase.futuremaster.common.test.spring;

import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.ConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.common.CommonConvertedFutureTestHelper;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SpringConvertedFutureTestHelper extends CommonConvertedFutureTestHelper implements ConvertedFutureTestHelper<ListenableFuture<String>> {
    private final ListenableFutureCallback<String> callback = mock(ListenableFutureCallback.class);

    @Override
    public void waitForCalculationToFinish(ListenableFuture<String> convertedFuture) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        convertedFuture.addCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                latch.countDown();
            }
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Override
    public void verifyCallbackCalledWithCorrectValue() {
        waitForCallback();
        verify(callback).onSuccess(AbstractConverterTest.VALUE);
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
    public void addCallbackTo(ListenableFuture<String> convertedFuture) {
        convertedFuture.addCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
                callbackCalled();
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
                callbackCalled();
            }
        });
    }
}
