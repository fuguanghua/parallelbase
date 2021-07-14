package me.fuguanghua.future.java8guava;

import com.google.common.util.concurrent.ListenableFuture;
import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterHelperBasedTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.guava.GuavaConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.java8.Java8OriginalFutureTestHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static me.fuguanghua.future.java8guava.FutureConverter.toCompletableFuture;
import static me.fuguanghua.future.java8guava.FutureConverter.toListenableFuture;


public class ToListenableFutureConverterTest extends AbstractConverterHelperBasedTest<
        CompletableFuture<String>,
        ListenableFuture<String>> {


    public ToListenableFutureConverterTest() {
        super(new Java8OriginalFutureTestHelper(), new GuavaConvertedFutureTestHelper());
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
