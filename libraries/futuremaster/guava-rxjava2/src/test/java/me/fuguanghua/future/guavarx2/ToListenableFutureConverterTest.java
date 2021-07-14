package me.fuguanghua.future.guavarx2;

import com.google.common.util.concurrent.ListenableFuture;
import io.reactivex.Single;
import me.fuguanghua.parallelbase.futuremaster.common.test.guava.GuavaConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.rxjava2.AbstractSingleToFutureConverterTest;

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
