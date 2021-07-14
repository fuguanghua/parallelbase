package me.fuguanghua.future.java8guava;

import com.google.common.util.concurrent.ListenableFuture;
import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterHelperBasedTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.guava.GuavaOriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.java8.Java8ConvertedFutureTestHelper;

import java.util.concurrent.CompletableFuture;

import static me.fuguanghua.future.java8guava.FutureConverter.toCompletableFuture;
import static me.fuguanghua.future.java8guava.FutureConverter.toListenableFuture;


public class ToCompletableFutureConverterTest extends AbstractConverterHelperBasedTest<
        ListenableFuture<String>,
        CompletableFuture<String>> {

    public ToCompletableFutureConverterTest() {
        super(new GuavaOriginalFutureTestHelper(), new Java8ConvertedFutureTestHelper());
    }

    @Override
    protected CompletableFuture<String> convert(ListenableFuture<String> originalFuture) {
        return toCompletableFuture(originalFuture);
    }

    @Override
    protected ListenableFuture<String> convertBack(CompletableFuture<String> converted) {
        return toListenableFuture(converted);
    }
}
