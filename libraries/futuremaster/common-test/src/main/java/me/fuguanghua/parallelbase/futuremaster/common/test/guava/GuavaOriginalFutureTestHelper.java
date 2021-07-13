package me.fuguanghua.parallelbase.futuremaster.common.test.guava;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.OriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.common.CommonOriginalFutureTestHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class GuavaOriginalFutureTestHelper extends CommonOriginalFutureTestHelper implements OriginalFutureTestHelper<ListenableFuture<String>> {
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    @Override
    public ListenableFuture<String> createFinishedFuture() {
        return Futures.immediateFuture(AbstractConverterTest.VALUE);
    }

    @Override
    public ListenableFuture<String> createRunningFuture() {
        return executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    waitForSignal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return AbstractConverterTest.VALUE;
            }
        });
    }

    @Override
    public ListenableFuture<String> createExceptionalFuture(Exception exception) {
        return Futures.immediateFailedFuture(exception);
    }
}
