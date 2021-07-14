package me.fuguanghua.future.guavarx2;

import com.google.common.util.concurrent.ListenableFuture;
import io.reactivex.Single;
import me.fuguanghua.parallelbase.futuremaster.common.test.guava.GuavaOriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.rxjava2.AbstractFutureToSingleConverterTest;

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
