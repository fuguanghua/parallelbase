package me.fuguanghua.future.java8rx;

import me.fuguanghua.parallelbase.futuremaster.common.test.java8.Java8OriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.rxjava.AbstractFutureToSingleConverterTest;
import rx.Single;

import java.util.concurrent.CompletableFuture;

public class ToSingleConverterTest extends AbstractFutureToSingleConverterTest<CompletableFuture<String>> {
    public ToSingleConverterTest() {
        super(new Java8OriginalFutureTestHelper());
    }

    @Override
    protected Single<String> toSingle(CompletableFuture<String> future) {
        return FutureConverter.toSingle(future);
    }

    @Override
    protected CompletableFuture<String> toFuture(Single<String> single) {
        return FutureConverter.toCompletableFuture(single);
    }
}
