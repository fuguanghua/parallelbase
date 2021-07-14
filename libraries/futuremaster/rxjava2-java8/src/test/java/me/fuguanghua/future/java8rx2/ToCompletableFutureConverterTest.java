package me.fuguanghua.future.java8rx2;

import io.reactivex.Single;
import me.fuguanghua.parallelbase.futuremaster.common.test.java8.Java8ConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.rxjava2.AbstractSingleToFutureConverterTest;

import java.util.concurrent.CompletableFuture;

public class ToCompletableFutureConverterTest extends AbstractSingleToFutureConverterTest<CompletableFuture<String>> {
    public ToCompletableFutureConverterTest() {
        super(new Java8ConvertedFutureTestHelper());
    }

    @Override
    protected CompletableFuture<String> toFuture(Single<String> single) {
        return FutureConverter.toCompletableFuture(single);
    }

    @Override
    protected Single<String> toSingle(CompletableFuture<String> future) {
        return FutureConverter.toSingle(future);
    }
}
