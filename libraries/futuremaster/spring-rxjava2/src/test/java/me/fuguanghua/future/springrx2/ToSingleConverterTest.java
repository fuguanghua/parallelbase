package me.fuguanghua.future.springrx2;

import io.reactivex.Single;
import me.fuguanghua.parallelbase.futuremaster.common.test.rxjava2.AbstractFutureToSingleConverterTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.spring.SpringOriginalFutureTestHelper;
import org.springframework.util.concurrent.ListenableFuture;

public class ToSingleConverterTest extends AbstractFutureToSingleConverterTest<ListenableFuture<String>> {

    public ToSingleConverterTest() {
        super(new SpringOriginalFutureTestHelper());
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
