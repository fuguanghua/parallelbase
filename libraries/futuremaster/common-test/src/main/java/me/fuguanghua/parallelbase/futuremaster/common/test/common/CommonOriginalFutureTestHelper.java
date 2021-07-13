package me.fuguanghua.parallelbase.futuremaster.common.test.common;

import java.util.concurrent.CountDownLatch;

public class CommonOriginalFutureTestHelper {
    private final CountDownLatch waitLatch = new CountDownLatch(1);

    protected void waitForSignal() throws InterruptedException {
        waitLatch.await();
    }

    public void finishRunningFuture() {
        waitLatch.countDown();
    }
}
