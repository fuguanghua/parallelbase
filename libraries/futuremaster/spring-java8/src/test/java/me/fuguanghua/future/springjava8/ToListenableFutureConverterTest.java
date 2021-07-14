package me.fuguanghua.future.springjava8;

import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterHelperBasedTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.java8.Java8OriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.spring.SpringConvertedFutureTestHelper;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static me.fuguanghua.future.springjava8.FutureConverter.toCompletableFuture;
import static me.fuguanghua.future.springjava8.FutureConverter.toListenableFuture;

public class ToListenableFutureConverterTest extends AbstractConverterHelperBasedTest<
        CompletableFuture<String>,
        ListenableFuture<String>> {


    public ToListenableFutureConverterTest() {
        super(new Java8OriginalFutureTestHelper(), new SpringConvertedFutureTestHelper());
    }

    @Override
    protected ListenableFuture<String> convert(CompletableFuture<String> originalFuture) {
        return toListenableFuture(originalFuture);
    }

    @Override
    protected CompletableFuture<String> convertBack(ListenableFuture<String> converted) {
        return toCompletableFuture(converted);
    }

    @Test
    @Ignore
    public void testCancelBeforeConversion() throws ExecutionException, InterruptedException {
        // completable futures can not be canceled
    }

}
