package me.fuguanghua.future.guavarx;

import com.google.common.util.concurrent.ListenableFuture;
import me.fuguanghua.future.rxjavacommon.RxJavaFutureUtils;
import me.fuguanghua.parallebase.future.guava.common.GuavaFutureUtils;
import rx.Single;

public class FutureConverter {

    /**
     * Converts {@link com.google.common.util.concurrent.ListenableFuture} to  {@link rx.Single}.
     * The original future is canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(ListenableFuture<T> listenableFuture) {
        return RxJavaFutureUtils.createSingle(GuavaFutureUtils.createValueSource(listenableFuture));
    }

    /**
     * Converts  {@link rx.Single} to {@link com.google.common.util.concurrent.ListenableFuture}.
     */
    public static <T> ListenableFuture<T> toListenableFuture(Single<T> single) {
        return GuavaFutureUtils.createListenableFuture(RxJavaFutureUtils.createValueSource(single));
    }
}
