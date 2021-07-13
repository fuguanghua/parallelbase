package me.fuguanghua.parallelbase.futuremaster.common.test.rxjava;

import me.fuguanghua.parallelbase.futuremaster.common.test.ConvertedFutureTestHelper;
import org.junit.After;
import org.junit.Test;
import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;
import rx.subscriptions.Subscriptions;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterTest.VALUE;
import static org.junit.Assert.*;

public abstract class AbstractSingleToFutureConverterTest<T extends Future<String>> {

    private final CountDownLatch waitLatch = new CountDownLatch(1);
    private final CountDownLatch taskStartedLatch = new CountDownLatch(1);

    private AtomicInteger subscribed = new AtomicInteger(0);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AtomicReference<Future> futureTaskRef = new AtomicReference<>();

    private final ConvertedFutureTestHelper<T> convertedFutureTestHelper;

    protected AbstractSingleToFutureConverterTest(ConvertedFutureTestHelper<T> convertedFutureTestHelper) {
        this.convertedFutureTestHelper = convertedFutureTestHelper;
    }

    protected abstract T toFuture(Single<String> single);

    protected abstract Single<String> toSingle(T future);

    @After
    public void cleanup() {
        waitLatch.countDown();
        executorService.shutdown();
    }


    @Test
    public void testConvertToFutureCompleted() throws ExecutionException, InterruptedException {
        Single<String> single = Single.just(VALUE);
        T future = toFuture(single);

        convertedFutureTestHelper.addCallbackTo(future);

        assertEquals(VALUE, future.get());
        assertEquals(true, future.isDone());
        assertEquals(false, future.isCancelled());
        convertedFutureTestHelper.verifyCallbackCalledWithCorrectValue();

        assertSame(single, toSingle(future));
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {
        Single<String> single = createAsyncSingle();
        T future = toFuture(single);

        assertEquals(false, future.isDone());
        assertEquals(false, future.isCancelled());

        convertedFutureTestHelper.addCallbackTo(future);
        waitLatch.countDown();

        //wait for the result
        assertEquals(VALUE, future.get());
        assertEquals(true, future.isDone());
        assertEquals(false, future.isCancelled());

        convertedFutureTestHelper.verifyCallbackCalledWithCorrectValue();
        assertEquals(1, subscribed.get());
    }

    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        Single<String> single = createAsyncSingle();

        T future = toFuture(single);

        taskStartedLatch.await(); //wait for the task to start
        getWorkerFuture().cancel(true);
        assertTrue(getWorkerFuture().isCancelled());

        try {
            future.get();
            fail("Exception expected");
        } catch (ExecutionException e) {
            //ok
        }
        assertEquals(true, future.isDone());
        assertEquals(false, future.isCancelled());

        assertEquals(1, subscribed.get());
    }

    @Test
    public void shouldEndExceptionallyIfObservableFailsBeforeConversion() throws InterruptedException {
        RuntimeException exception = new RuntimeException("test");
        Single<String> single = Single.error(exception);


        T future = toFuture(single);

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        try {
            future.get();
            fail("Exception expected");
        } catch (ExecutionException e) {
            assertSame(exception, e.getCause());
        }
    }

    @Test
    public void testCancelNew() throws ExecutionException, InterruptedException {
        Single<String> single = createAsyncSingle();

        T future = toFuture(single);
        assertTrue(future.cancel(true));

        try {
            future.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, future.isDone());
        assertEquals(true, future.isCancelled());


        assertEquals(1, subscribed.get());
    }

    @Test
    public void cancelShouldUnsubscribe() {
        TestSubject<String> single = TestSubject.create(new TestScheduler());
        assertFalse(single.hasObservers());

        T future = toFuture(single.toSingle());
        assertTrue(single.hasObservers());

        future.cancel(true);

        assertFalse(single.hasObservers());
    }


    @Test
    public void testCancelCompleted() throws ExecutionException, InterruptedException {
        Single<String> single = Single.just(VALUE);

        T future = toFuture(single);
        assertFalse(future.cancel(true));

        assertEquals(VALUE, future.get());

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
    }

    @Test
    public void testRuntimeException() throws ExecutionException, InterruptedException {
        doTestException(new RuntimeException("test"));
    }

    @Test
    public void testIOException() throws ExecutionException, InterruptedException {
        doTestException(new IOException("test"));
    }


    private void doTestException(final Exception exception) throws ExecutionException, InterruptedException {
        Single<String> single = Single.create((SingleSubscriber<? super String> subscriber) -> subscriber.onError(exception));

        T future = toFuture(single);
        try {
            future.get();
        } catch (ExecutionException e) {
            assertSame(exception, e.getCause());
        }
    }


    private Single<String> createAsyncSingle() {
        return Single.create((SingleSubscriber<? super String> subscriber) -> {
            subscribed.incrementAndGet();
            Future<?> future = executorService.submit(() -> {
                try {
                    taskStartedLatch.countDown();
                    waitLatch.await();
                    subscriber.onSuccess(VALUE);
                } catch (InterruptedException e) {
                    subscriber.onError(e);
                    throw new RuntimeException(e);
                }
            });
            subscriber.add(Subscriptions.from(future));
            assertTrue(this.futureTaskRef.compareAndSet(null, future));
        });
    }

    /**
     * Future that is running underneath the Observable.
     *
     * @return
     */
    protected Future getWorkerFuture() {
        return futureTaskRef.get();
    }
}
