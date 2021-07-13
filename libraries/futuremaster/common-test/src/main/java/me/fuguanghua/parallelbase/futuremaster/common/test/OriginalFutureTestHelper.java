package me.fuguanghua.parallelbase.futuremaster.common.test;


import java.util.concurrent.Future;

public interface OriginalFutureTestHelper<F extends Future<String>> {

    F createFinishedFuture();

    F createRunningFuture();

    F createExceptionalFuture(Exception exception);

    void finishRunningFuture();
}
