package me.fuguanghua.future.springjava8;

import me.fuguanghua.future.java8.common.Java8FutureUtils;
import me.fuguanghua.future.springcommon.SpringFutureUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

/**
 * Converts between {@link java.util.concurrent.CompletableFuture} and Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}.
 */
public class FutureConverter {
    /**
     * Converts {@link java.util.concurrent.CompletableFuture} to {@link org.springframework.util.concurrent.ListenableFuture}.
     */
    public static <T> ListenableFuture<T> toListenableFuture(CompletableFuture<T> completableFuture) {
        return SpringFutureUtils.createListenableFuture(Java8FutureUtils.createValueSourceFuture(completableFuture));
    }

    /**
     * Converts  {@link org.springframework.util.concurrent.ListenableFuture} to {@link java.util.concurrent.CompletableFuture}.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
        return Java8FutureUtils.createCompletableFuture(SpringFutureUtils.createValueSourceFuture(listenableFuture));
    }
}
