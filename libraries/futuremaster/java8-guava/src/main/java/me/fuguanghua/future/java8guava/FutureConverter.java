package me.fuguanghua.future.java8guava;

import com.google.common.util.concurrent.ListenableFuture;
import me.fuguanghua.future.java8.common.Java8FutureUtils;
import me.fuguanghua.parallebase.future.guava.common.GuavaFutureUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Converts between {@link java.util.concurrent.CompletableFuture} and Guava {@link com.google.common.util.concurrent.ListenableFuture}.
 */
public class FutureConverter {
    /**
     * Converts {@link java.util.concurrent.CompletableFuture} to {@link com.google.common.util.concurrent.ListenableFuture}.
     */
    public static <T> ListenableFuture<T> toListenableFuture(CompletableFuture<T> completableFuture) {
        return GuavaFutureUtils.createListenableFuture(Java8FutureUtils.createValueSourceFuture(completableFuture));
    }

    /**
     * Converts  {@link com.google.common.util.concurrent.ListenableFuture} to {@link java.util.concurrent.CompletableFuture}.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
        return Java8FutureUtils.createCompletableFuture(GuavaFutureUtils.createValueSourceFuture(listenableFuture));
    }
}
