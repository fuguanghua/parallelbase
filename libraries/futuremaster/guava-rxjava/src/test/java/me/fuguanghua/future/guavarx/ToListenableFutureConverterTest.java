package me.fuguanghua.future.guavarx;

import com.google.common.util.concurrent.ListenableFuture;
import me.fuguanghua.parallelbase.futuremaster.common.test.guava.GuavaConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.rxjava.AbstractSingleToFutureConverterTest;
import rx.Single;

public class ToListenableFutureConverterTest extends AbstractSingleToFutureConverterTest<ListenableFuture<String>> {

    public ToListenableFutureConverterTest() {
        super(new GuavaConvertedFutureTestHelper());
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
