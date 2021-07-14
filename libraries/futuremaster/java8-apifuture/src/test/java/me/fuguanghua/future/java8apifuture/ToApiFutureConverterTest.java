package me.fuguanghua.future.java8apifuture;

import com.google.api.core.ApiFuture;
import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterHelperBasedTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.apicommon.ApiCommonConvertedFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.java8.Java8OriginalFutureTestHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static me.fuguanghua.future.java8apifuture.FutureConverter.toApiFuture;
import static me.fuguanghua.future.java8apifuture.FutureConverter.toCompletableFuture;


public class ToApiFutureConverterTest extends AbstractConverterHelperBasedTest<
        CompletableFuture<String>,
        ApiFuture<String>> {


    public ToApiFutureConverterTest() {
        super(new Java8OriginalFutureTestHelper(), new ApiCommonConvertedFutureTestHelper());
    }

    @Override
    protected ApiFuture<String> convert(CompletableFuture<String> originalFuture) {
        return toApiFuture(originalFuture);
    }

    @Override
    protected CompletableFuture<String> convertBack(ApiFuture<String> converted) {
        return toCompletableFuture(converted);
    }

    @Test
    @Ignore
    public void testCancelBeforeConversion() throws ExecutionException, InterruptedException {
        // completable futures can not be canceled
    }

}
