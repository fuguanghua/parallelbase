package me.fuguanghua.parallelbase.futuremaster.common.test.common;

import java.util.concurrent.CountDownLatch;

public class CommonConvertedFutureTestHelper {
    // latch to wait for callback to be called
    private final CountDownLatch callbackLatch = new CountDownLatch(1);

    protected void callbackCalled() {
        callbackLatch.countDown();
    }

    protected void waitForCallback() {
        try {
            callbackLatch.await();
        } catch (InterruptedException e) {
            // ok
        }
    }
}
