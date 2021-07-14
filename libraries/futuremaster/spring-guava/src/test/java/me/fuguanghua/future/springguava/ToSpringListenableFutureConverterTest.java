package me.fuguanghua.future.springguava;

import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterHelperBasedTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.guava.GuavaOriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.spring.SpringConvertedFutureTestHelper;
import org.springframework.util.concurrent.ListenableFuture;

import static me.fuguanghua.future.springguava.FutureConverter.toGuavaListenableFuture;
import static me.fuguanghua.future.springguava.FutureConverter.toSpringListenableFuture;

public class ToSpringListenableFutureConverterTest extends AbstractConverterHelperBasedTest<
        com.google.common.util.concurrent.ListenableFuture<String>,
        ListenableFuture<String>> {

    public ToSpringListenableFutureConverterTest() {
        super(new GuavaOriginalFutureTestHelper(), new SpringConvertedFutureTestHelper());
    }

    @Override
    protected ListenableFuture<String> convert(com.google.common.util.concurrent.ListenableFuture<String> originalFuture) {
        return toSpringListenableFuture(originalFuture);
    }

    @Override
    protected com.google.common.util.concurrent.ListenableFuture<String> convertBack(ListenableFuture<String> converted) {
        return toGuavaListenableFuture(converted);
    }

}
