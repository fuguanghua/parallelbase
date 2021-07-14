package me.fuguanghua.future.springrxjava;

import me.fuguanghua.parallelbase.futuremaster.common.test.rxjava.AbstractSingleToFutureConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.spring.SpringConvertedFutureTestHelper;
import org.springframework.util.concurrent.ListenableFuture;
import rx.Single;

public class ToListenableFutureConverterTest extends AbstractSingleToFutureConverterTest<ListenableFuture<String>> {

    public ToListenableFutureConverterTest() {
        super(new SpringConvertedFutureTestHelper());
    }

    @Override
    protected ListenableFuture<String> toFuture(Single<String> single) {
        return FutureConverter.toListenableFuture(single);
    }

    @Override
    protected Single<String> toSingle(ListenableFuture<String> future) {
        return FutureConverter.toSingle(future);
    }
}
