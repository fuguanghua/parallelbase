package me.fuguanghua.future.java8apifuture;

import com.google.api.core.ApiFuture;
import me.fuguanghua.parallelbase.futuremaster.common.test.AbstractConverterHelperBasedTest;
import me.fuguanghua.parallelbase.futuremaster.common.test.apicommon.ApiCommonOriginalFutureTestHelper;
import me.fuguanghua.parallelbase.futuremaster.common.test.java8.Java8ConvertedFutureTestHelper;

import java.util.concurrent.CompletableFuture;

import static me.fuguanghua.future.java8apifuture.FutureConverter.toApiFuture;
import static me.fuguanghua.future.java8apifuture.FutureConverter.toCompletableFuture;


public class ToCompletableFutureConverterTest extends AbstractConverterHelperBasedTest<
    ApiFuture<String>, CompletableFuture<String>> {

    public ToCompletableFutureConverterTest() {
        super(new ApiCommonOriginalFutureTestHelper(), new Java8ConvertedFutureTestHelper());
    }

    @Override
    protected CompletableFuture<String> convert(ApiFuture<String> originalFuture) {
        return toCompletableFuture(originalFuture);
    }

    @Override
    protected ApiFuture<String> convertBack(CompletableFuture<String> converted) {
        return toApiFuture(converted);
    }
}
