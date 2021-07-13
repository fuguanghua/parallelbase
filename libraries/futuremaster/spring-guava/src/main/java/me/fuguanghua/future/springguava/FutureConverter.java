package me.fuguanghua.future.springguava;

import me.fuguanghua.future.springcommon.SpringFutureUtils;
import me.fuguanghua.parallebase.future.guava.common.GuavaFutureUtils;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * Converts between Guava {@link com.google.common.util.concurrent.ListenableFuture} and Spring 5 {@link org.springframework.util.concurrent.ListenableFuture}.
 */
public class FutureConverter {
    /**
     * Converts Guava {@link com.google.common.util.concurrent.ListenableFuture} to Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}
     */
    public static <T> ListenableFuture<T> toSpringListenableFuture(com.google.common.util.concurrent.ListenableFuture<T> guavaListenableFuture) {
        return SpringFutureUtils.createListenableFuture(GuavaFutureUtils.createValueSourceFuture(guavaListenableFuture));
    }

    /**
     * Converts Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}
     * to Guava {@link com.google.common.util.concurrent.ListenableFuture}.
     */
    public static <T> com.google.common.util.concurrent.ListenableFuture<T> toGuavaListenableFuture(ListenableFuture<T> springListenableFuture) {
        return GuavaFutureUtils.createListenableFuture(SpringFutureUtils.createValueSourceFuture(springListenableFuture));
    }
}
