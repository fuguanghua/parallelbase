package me.fuguanghua.parallelbase.futuremaster.common.test.java8;

import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.OriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.common.CommonOriginalFutureTestHelper;

import java.util.concurrent.CompletableFuture;

public class Java8OriginalFutureTestHelper extends CommonOriginalFutureTestHelper implements OriginalFutureTestHelper<CompletableFuture<String>> {

    @Override
    public CompletableFuture<String> createFinishedFuture() {
        return CompletableFuture.completedFuture(AbstractConverterTest.VALUE);
    }

    @Override
    public CompletableFuture<String> createRunningFuture() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                waitForSignal();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return AbstractConverterTest.VALUE;
        });
    }

    @Override
    public CompletableFuture<String> createExceptionalFuture(Exception exception) {
        CompletableFuture<String> completable = new CompletableFuture<>();
        completable.completeExceptionally(exception);
        return completable;
    }

}
