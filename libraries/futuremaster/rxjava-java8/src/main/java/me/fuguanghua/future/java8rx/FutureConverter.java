package me.fuguanghua.future.java8rx;

import me.fuguanghua.future.java8.common.Java8FutureUtils;
import me.fuguanghua.future.rxjavacommon.RxJavaFutureUtils;
import rx.Single;

import java.util.concurrent.CompletableFuture;

/**
 * Converts between Java 8 {@link CompletableFuture} and RxJava {@link Single}
 */
public class FutureConverter {

    /**
     * Converts {@link Single} to {@link CompletableFuture}.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(Single<T> single) {
        return Java8FutureUtils.createCompletableFuture(RxJavaFutureUtils.createValueSource(single));
    }

    /**
     * Converts {@link CompletableFuture} to {@link Single}.
     * The original future is canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(CompletableFuture<T> completableFuture) {
        return RxJavaFutureUtils.createSingle(Java8FutureUtils.createValueSource(completableFuture));
    }
}

