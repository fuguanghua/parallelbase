package me.fuguanghua.parallelbase.futuremaster.common.test.apicommon;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.OriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.common.CommonOriginalFutureTestHelper;

import java.util.concurrent.Executors;

public class ApiCommonOriginalFutureTestHelper extends CommonOriginalFutureTestHelper implements OriginalFutureTestHelper<ApiFuture<String>> {
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    @Override
    public ApiFuture<String> createFinishedFuture() {
        return ApiFutures.immediateFuture(AbstractConverterTest.VALUE);
    }

    @Override
    public ApiFuture<String> createRunningFuture() {
        return ApiFutures.transform(
            ApiFutures.immediateFuture(""),
            input -> {
                try {
                    waitForSignal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return AbstractConverterTest.VALUE;
            },
            executorService);
    }

    @Override
    public ApiFuture<String> createExceptionalFuture(Exception exception) {
        return ApiFutures.immediateFailedFuture(exception);
    }
}
