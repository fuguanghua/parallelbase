package me.fuguanghua.parallelbase.futuremaster.common.test.rxjava2;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.fuguanghua.parallelbase.futuremaster.common.test.OriginalFutureTestHelper;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

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
    public void testConvertToSingleFinished() throws Exception {
        T completable = originalFutureTestHelper.createFinishedFuture();

        Single<String> single = toSingle(completable);
        Consumer<String> onSuccess = mockAction();
        Consumer<Throwable> onError = mockAction();

        single.subscribe(v -> {
                onSuccess.accept(v);
                latch.countDown();
            },
            onError);

        latch.await();

        verify(onSuccess).accept(VALUE);
        verifyZeroInteractions(onError);

        assertSame(completable, toFuture(single));
    }

    @Test
    public void testRun() throws Exception {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        Consumer<String> onSuccess = mockAction();
        Consumer<Throwable> onError = mockAction();

        single.subscribe(v -> {
                onSuccess.accept(v);
                latch.countDown();
            },
            onError);
        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);

        originalFutureTestHelper.finishRunningFuture();
        latch.await();

        //wait for the result
        verify(onSuccess).accept(VALUE);
        verifyZeroInteractions(onError);
    }

    @Test
    public void testMultipleSubscribers() throws Exception {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        CountDownLatch latch = new CountDownLatch(2);

        Consumer<String> onSuccess1 = mockAction();
        Consumer<Throwable> onError1 = mockAction();

        // first subscription
        single.subscribe(v -> {
                onSuccess1.accept(v);
                latch.countDown();
            },
            onError1);
        verifyZeroInteractions(onSuccess1);
        verifyZeroInteractions(onError1);

        // second subscription
        Consumer<String> onSuccess2 = mockAction();
        Consumer<Throwable> onError2 = mockAction();


        single.subscribe(v -> {
                onSuccess2.accept(v);
                latch.countDown();
            },
            onError2);
        verifyZeroInteractions(onSuccess2);
        verifyZeroInteractions(onError2);


        originalFutureTestHelper.finishRunningFuture();

        //wait for the result
        latch.await();

        verify(onSuccess1).accept(VALUE);
        verifyZeroInteractions(onError1);

        verify(onSuccess2).accept(VALUE);
        verifyZeroInteractions(onError2);
    }

    @Test
    public void unsubscribeShouldCancelTheFuture() throws InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        Consumer<String> onSuccess = mockAction();
        Consumer<Throwable> onError = mockAction();

        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);

        single.subscribe(v -> {}).dispose();

        assertTrue(future.isCancelled());

        //wait for the result
        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);
    }

    @Test
    public void oneSubscriptionShouldNotCancelFuture() throws Exception {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future).toObservable().publish().refCount().singleOrError();
        Consumer<String> onSuccess = mockAction();
        Consumer<Throwable> onError = mockAction();

        single.subscribe(v -> {
            onSuccess.accept(v);
            latch.countDown();
        }, onError);
        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);

        single.subscribe(v -> {}).dispose();

        originalFutureTestHelper.finishRunningFuture();
        latch.await();

        //wait for the result
        verify(onSuccess).accept(VALUE);
        verifyZeroInteractions(onError);
    }

    @Test
    public void testCancelOriginal() throws Exception {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        Consumer<String> onNext = mockAction();
        final Consumer<Throwable> onError = mockAction();

        single.subscribe(
            onNext,
            t -> {
                onError.accept(t);
                latch.countDown();
            }
        );
        future.cancel(true);

        latch.await();

        verify(onError).accept(any(Throwable.class));
        verifyZeroInteractions(onNext);
    }

    @SuppressWarnings("unchecked")
    private <S> Consumer<S> mockAction() {
        return mock(Consumer.class);
    }

    @Test
    public void testUnsubscribe() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Single<String> single = toSingle(future);
        Consumer<String> onSuccess = mockAction();
        Consumer<Throwable> onError = mockAction();


        Disposable disposable = single.subscribe(
            onSuccess,
            onError
        );

        disposable.dispose();
        assertTrue(disposable.isDisposed());

        originalFutureTestHelper.finishRunningFuture();
        Thread.sleep(10); //do not know how to wait for something to not happen

        verifyZeroInteractions(onSuccess);
        verifyZeroInteractions(onError);
    }

    @Test
    @Ignore //RxJava 2 swallows the exception
    public void shouldPropagateExceptionFromObserver() throws Exception {
        T future = originalFutureTestHelper.createFinishedFuture();

        Single<String> single = toSingle(future);
        Consumer<Throwable> onError = mockAction();
        RuntimeException exception = new RuntimeException("Test");
        single.subscribe(val -> {
                throw exception;
            },
            onError
        );

        verify(onError).accept(exception);
    }

    @Test
    public void testRethrowException() throws Exception {
        doTestException(new RuntimeException("test"));
    }

    private void doTestException(final RuntimeException exception) throws Exception {
        T future = originalFutureTestHelper.createExceptionalFuture(exception);

        Single<String> single = toSingle(future);
        Consumer<String> onSuccess = mockAction();
        final Consumer<Throwable> onError = mockAction();

        single.subscribe(
            onSuccess,
            t -> {
                onError.accept(t);
                latch.countDown();
            }
        );
        latch.await();

        //wait for the result
        verifyZeroInteractions(onSuccess);
        verify(onError).accept(exception);
    }
}
