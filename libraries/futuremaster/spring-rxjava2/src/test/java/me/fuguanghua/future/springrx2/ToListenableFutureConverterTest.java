package me.fuguanghua.future.springrx2;

import io.reactivex.Single;
import me.fuguanghua.parallelbase.futuremaster.common.test.rxjava2.AbstractSingleToFutureConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.spring.SpringConvertedFutureTestHelper;
import org.springframework.util.concurrent.ListenableFuture;

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
