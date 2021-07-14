package me.fuguanghua.future.springguava;

import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterHelperBasedTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.guava.GuavaConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.spring.SpringOriginalFutureTestHelper;
import org.springframework.util.concurrent.ListenableFuture;

import static me.fuguanghua.future.springguava.FutureConverter.toGuavaListenableFuture;
import static me.fuguanghua.future.springguava.FutureConverter.toSpringListenableFuture;

public class ToGuavaListenableFutureConverterTest extends AbstractConverterHelperBasedTest<
        ListenableFuture<String>,
        com.google.common.util.concurrent.ListenableFuture<String>> {

    public ToGuavaListenableFutureConverterTest() {
        super(new SpringOriginalFutureTestHelper(), new GuavaConvertedFutureTestHelper());
    }

    @Override
    protected com.google.common.util.concurrent.ListenableFuture<String> convert(ListenableFuture<String> originalFuture) {
        return toGuavaListenableFuture(originalFuture);
    }

    @Override
    protected ListenableFuture<String> convertBack(com.google.common.util.concurrent.ListenableFuture<String> converted) {
        return toSpringListenableFuture(converted);
    }
}
