package me.fuguanghua.future.java8apifuture;

import com.google.api.core.ApiFuture;
import me.fuguanghua.future.java8.common.Java8FutureUtils;
import me.fuguanghua.parallelbase.apifuture.ApiFutureUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Converts between {@link java.util.concurrent.CompletableFuture} and Google {@link com.google.api.core.ApiFuture}.
 */
public class FutureConverter {
    /**
     * Converts {@link java.util.concurrent.CompletableFuture} to {@link com.google.api.core.ApiFuture}.
     */
    public static <T> ApiFuture<T> toApiFuture(CompletableFuture<T> completableFuture) {
        return ApiFutureUtils.createApiFuture(Java8FutureUtils.createValueSourceFuture(completableFuture));
    }

    /**
     * Converts  {@link com.google.api.core.ApiFuture} to {@link java.util.concurrent.CompletableFuture}.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
        return Java8FutureUtils.createCompletableFuture(ApiFutureUtils.createValueSourceFuture(apiFuture));
    }
}
