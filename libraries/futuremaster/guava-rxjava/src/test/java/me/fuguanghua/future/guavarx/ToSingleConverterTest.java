package me.fuguanghua.future.guavarx;

import com.google.common.util.concurrent.ListenableFuture;
import me.fuguanghua.parallelbase.futuremaster.common.test.guava.GuavaOriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.rxjava.AbstractFutureToSingleConverterTest;
import rx.Single;

public class ToSingleConverterTest extends AbstractFutureToSingleConverterTest<ListenableFuture<String>> {

    public ToSingleConverterTest() {
        super(new GuavaOriginalFutureTestHelper());
    }

    @Override
    protected Single<String> toSingle(ListenableFuture<String> future) {
        return FutureConverter.toSingle(future);
    }

    @Override
    protected ListenableFuture<String> toFuture(Single<String> single) {
        return FutureConverter.toListenableFuture(single);
    }
}
