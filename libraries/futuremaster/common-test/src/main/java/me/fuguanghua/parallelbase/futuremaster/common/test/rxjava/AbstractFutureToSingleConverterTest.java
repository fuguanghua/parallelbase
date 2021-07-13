package me.fuguanghua.parallelbase.futuremaster.common.test.rxjava;

import me.fuguanghua.parallelbase.futuremaster.common.test.OriginalFutureTestHelper;
import org.junit.After;
import org.junit.Test;
import rx.Single;
import rx.Subscription;
import rx.functions.Action1;

import java.util.concurrent.*;

import static me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterTest.VALUE;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractFutureToSingleConverterTest<T extends Future<String>> {


    private final CountDownLatch latch = new CountDownLatch(1);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final OriginalFutureTestHelper<T> originalFutureTestHelper;

    protected AbstractFutureToSingleConverterTest(OriginalFutureTestHelper<T> originalFutureTestHelper) {
        this.originalFutureTestHelper = originalFutureTestHelper;
    }

    protected abstract Single<String> toSingle(T future);

    protected abstract T toFuture(Single<String> single);

    @After
    public void cleanup() {
        executorService.shutdown();
    }

    @Test
    public void testConvertToSingleFinished() throws ExecutionException, InterruptedException {
        T completable = originalFutureTestHelper.createFinishedFuture();

        Single<String> single = toSingle(completable);
        Action1<String> onSuccess = mockAction();
        Action1<Throwable> onError = mockAction();

        single.subscribe(v -> {
                onSuccess.call(v);
                latch.countDown();
            },
            onError);

        latch.await();

        verify(onSuccess).call(VALUE);
        verifyZeroInteractions(onError);

        assertSame(completable, toFuture(single));
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        Action1<String> onSuccess = mockAction();
        Action1<Throwable> onError = mockAction();

        single.subscribe(v -> {
                onSuccess.call(v);
                latch.countDown();
            },
            onError);
        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);

        originalFutureTestHelper.finishRunningFuture();
        latch.await();

        //wait for the result
        verify(onSuccess).call(VALUE);
        verifyZeroInteractions(onError);
    }

    @Test
    public void testMultipleSubscribers() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        CountDownLatch latch = new CountDownLatch(2);

        Action1<String> onSuccess1 = mockAction();
        Action1<Throwable> onError1 = mockAction();

        // first subscription
        single.subscribe(v -> {
                onSuccess1.call(v);
                latch.countDown();
            },
            onError1);
        verifyZeroInteractions(onSuccess1);
        verifyZeroInteractions(onError1);

        // second subscription
        Action1<String> onSuccess2 = mockAction();
        Action1<Throwable> onError2 = mockAction();


        single.subscribe(v -> {
                onSuccess2.call(v);
                latch.countDown();
            },
            onError2);
        verifyZeroInteractions(onSuccess2);
        verifyZeroInteractions(onError2);


        originalFutureTestHelper.finishRunningFuture();

        //wait for the result
        latch.await();

        verify(onSuccess1).call(VALUE);
        verifyZeroInteractions(onError1);

        verify(onSuccess2).call(VALUE);
        verifyZeroInteractions(onError2);
    }

    @Test
    public void unsubscribeShouldCancelTheFuture() throws InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        Action1<String> onSuccess = mockAction();
        Action1<Throwable> onError = mockAction();

        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);

        single.subscribe(v -> {
        }).unsubscribe();

        assertTrue(future.isCancelled());

        //wait for the result
        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);
    }

    @Test
    public void oneSubscriptionShouldNotCancelFuture() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future).toObservable().publish().refCount().toSingle();
        Action1<String> onSuccess = mockAction();
        Action1<Throwable> onError = mockAction();

        single.subscribe(v -> {
            onSuccess.call(v);
            latch.countDown();
        }, onError);
        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);

        single.subscribe(v -> {
        }).unsubscribe();

        originalFutureTestHelper.finishRunningFuture();
        latch.await();

        //wait for the result
        verify(onSuccess).call(VALUE);
        verifyZeroInteractions(onError);
    }

    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        Action1<String> onNext = mockAction();
        final Action1<Throwable> onError = mockAction();

        single.subscribe(
            onNext,
            t -> {
                onError.call(t);
                latch.countDown();
            }
        );
        future.cancel(true);

        latch.await();

        verify(onError).call(any(Throwable.class));
        verifyZeroInteractions(onNext);
    }

    @SuppressWarnings("unchecked")
    private <S> Action1<S> mockAction() {
        return mock(Action1.class);
    }

    @Test
    public void testUnsubscribe() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        Action1<String> onSuccess = mockAction();
        Action1<Throwable> onError = mockAction();

        Subscription subscription = single.subscribe(
            onSuccess,
            onError
        );

        subscription.unsubscribe();
        assertTrue(subscription.isUnsubscribed());

        originalFutureTestHelper.finishRunningFuture();
        Thread.sleep(10); //do not know how to wait for something to not happen

        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);
    }

    @Test
    public void shouldPropagateExceptionFromObserver() {
        T future = originalFutureTestHelper.createFinishedFuture();

        Single<String> single = toSingle(future);
        Action1<Throwable> onError = mockAction();
        RuntimeException exception = new RuntimeException("Test");
        single.subscribe(val -> {
                throw exception;
            },
            onError
        );

        verify(onError).call(exception);
    }

    @Test
    public void testRethrowException() throws ExecutionException, InterruptedException {
        doTestException(new RuntimeException("test"));
    }

    private void doTestException(final RuntimeException exception) throws InterruptedException {
        T future = originalFutureTestHelper.createExceptionalFuture(exception);

        Single<String> single = toSingle(future);
        Action1<String> onSuccess = mockAction();
        final Action1<Throwable> onError = mockAction();

        single.subscribe(
            onSuccess,
            t -> {
                onError.call(t);
                latch.countDown();
            }
        );
        latch.await();

        //wait for the result
        verifyZeroInteractions(onSuccess);
        verify(onError).call(exception);
    }
}
