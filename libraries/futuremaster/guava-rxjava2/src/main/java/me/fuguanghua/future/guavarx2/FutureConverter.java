package me.fuguanghua.future.guavarx2;

import com.google.common.util.concurrent.ListenableFuture;
import io.reactivex.Single;
import me.fuguanghua.future.rxjava2common.RxJava2FutureUtils;
import me.fuguanghua.parallebase.future.guava.common.GuavaFutureUtils;

public class FutureConverter {

    /**
     * Converts {@link ListenableFuture} to  {@link Single}.
     * The original future is canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(ListenableFuture<T> listenableFuture) {
        return RxJava2FutureUtils.createSingle(GuavaFutureUtils.createValueSource(listenableFuture));
    }

    /**
     * Converts  {@link Single} to {@link ListenableFuture}.
     */
    public static <T> ListenableFuture<T> toListenableFuture(Single<T> single) {
        return GuavaFutureUtils.createListenableFuture(RxJava2FutureUtils.createValueSource(single));
    }
}
