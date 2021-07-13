package me.fuguanghua.future.java8rx2;

import io.reactivex.Single;
import me.fuguanghua.future.java8.common.Java8FutureUtils;
import me.fuguanghua.future.rxjava2common.RxJava2FutureUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Converts between Java 8 {@link CompletableFuture} and RxJava {@link io.reactivex.Single}
 */
public class FutureConverter {

    /**
     * Converts {@link io.reactivex.Single} to {@link CompletableFuture}.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(Single<T> single) {
        return Java8FutureUtils.createCompletableFuture(RxJava2FutureUtils.createValueSource(single));
    }

    /**
     * Converts {@link CompletableFuture} to {@link io.reactivex.Single}.
     * The original future is canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(CompletableFuture<T> completableFuture) {
        return RxJava2FutureUtils.createSingle(Java8FutureUtils.createValueSource(completableFuture));
    }
}

