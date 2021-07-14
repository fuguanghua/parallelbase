package me.fuguanghua.future.java8apifuture;

import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.core.Is.is;

public class ExceptionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testFinishedRuntimeExceptionStageToApi() throws ExecutionException, InterruptedException {
        IllegalStateException inputException = new IllegalStateException("something went wrong");
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(inputException);

        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(is(inputException));
        FutureConverter.toApiFuture(future)
                .get();
    }

    @Test
    public void testFinishedCheckedExceptionStageToApi() throws ExecutionException, InterruptedException {
        Exception inputException = new Exception("something went wrong");
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(inputException);

        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(is(inputException));
        FutureConverter.toApiFuture(future)
                .get();
    }

    @Test
    public void testApplyRuntimeExceptionStageToApi() throws ExecutionException, InterruptedException {
        IllegalStateException inputException = new IllegalStateException("something went wrong");
        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(is(inputException));

        FutureConverter.toApiFuture(
                CompletableFuture.completedFuture("")
                        .thenApply(s -> {
                            throw inputException;
                        }))
                .get();
    }

    @Test
    public void testApplyCheckedExceptionStageToApi() throws ExecutionException, InterruptedException {
        Exception inputException = new Exception("something went wrong");
        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(is(inputException));

        FutureConverter.toApiFuture(
                CompletableFuture.completedFuture("")
                        .thenCompose(s -> {
                            CompletableFuture<String> composed = new CompletableFuture<>();
                            composed.completeExceptionally(inputException);
                            return composed;
                        }))
                .get();
    }

    @Test
    public void testFinishedRuntimeExceptionApiToStage() {
        IllegalStateException inputException = new IllegalStateException("something went wrong");
        expectedException.expect(CompletionException.class);
        expectedException.expectCause(is(inputException));

        FutureConverter.toCompletableFuture(
                ApiFutures.immediateFailedFuture(inputException))
                .join();
    }

    @Test
    public void testFinishedCheckedExceptionApiToStage() {
        Exception inputException = new Exception("something went wrong");
        expectedException.expect(CompletionException.class);
        expectedException.expectCause(is(inputException));

        FutureConverter.toCompletableFuture(
                ApiFutures.immediateFailedFuture(inputException))
                .join();
    }

    @Test
    public void testApplyRuntimeExceptionApiToStage() {
        IllegalStateException inputException = new IllegalStateException("something went wrong");
        expectedException.expect(CompletionException.class);
        expectedException.expectCause(is(inputException));

        FutureConverter.toCompletableFuture(
                ApiFutures.transform(
                        ApiFutures.immediateFuture(""),
                        s -> {
                            throw inputException;
                        },
                        MoreExecutors.directExecutor()))
                .join();
    }

    @Test
    public void testApplyCheckedExceptionApiToStage() {
        Exception inputException = new Exception("something went wrong");
        expectedException.expect(CompletionException.class);
        expectedException.expectCause(is(inputException));

        FutureConverter.toCompletableFuture(
                ApiFutures.transformAsync(
                        ApiFutures.immediateFuture(""),
                        s -> ApiFutures.immediateFailedFuture(inputException),
                        MoreExecutors.directExecutor()))
                .join();
    }

}
