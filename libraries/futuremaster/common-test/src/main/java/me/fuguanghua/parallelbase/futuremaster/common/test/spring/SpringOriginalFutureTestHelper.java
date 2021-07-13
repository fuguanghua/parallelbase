package me.fuguanghua.parallelbase.futuremaster.common.test.spring;

import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.OriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.common.CommonOriginalFutureTestHelper;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class SpringOriginalFutureTestHelper extends CommonOriginalFutureTestHelper implements OriginalFutureTestHelper<ListenableFuture<String>> {

    private final AsyncListenableTaskExecutor executor = new TaskExecutorAdapter(Executors.newCachedThreadPool());

    @Override
    public ListenableFuture<String> createExceptionalFuture(final Exception exception) {
        return executor.submitListenable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw exception;
            }
        });
    }

    @Override
    public ListenableFuture<String> createFinishedFuture() {
        SettableListenableFuture<String> future = new SettableListenableFuture<>();
        future.set(AbstractConverterTest.VALUE);
        return future;
    }

    @Override
    public ListenableFuture<String> createRunningFuture() {
        return executor.submitListenable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                waitForSignal();
                return AbstractConverterTest.VALUE;
            }
        });
    }
}
