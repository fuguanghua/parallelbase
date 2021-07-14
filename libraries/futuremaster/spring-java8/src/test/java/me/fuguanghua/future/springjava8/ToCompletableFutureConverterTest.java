package me.fuguanghua.future.springjava8;

import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterHelperBasedTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.java8.Java8ConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.spring.SpringOriginalFutureTestHelper;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

import static me.fuguanghua.future.springjava8.FutureConverter.toCompletableFuture;
import static me.fuguanghua.future.springjava8.FutureConverter.toListenableFuture;

public class ToCompletableFutureConverterTest extends AbstractConverterHelperBasedTest<
        ListenableFuture<String>,
        CompletableFuture<String>> {

    public ToCompletableFutureConverterTest() {
        super(new SpringOriginalFutureTestHelper(), new Java8ConvertedFutureTestHelper());
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
