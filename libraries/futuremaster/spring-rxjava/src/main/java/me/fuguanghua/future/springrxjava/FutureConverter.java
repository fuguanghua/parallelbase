package me.fuguanghua.future.springrxjava;

import me.fuguanghua.future.rxjavacommon.RxJavaFutureUtils;
import me.fuguanghua.future.springcommon.SpringFutureUtils;
import org.springframework.util.concurrent.ListenableFuture;
import rx.Single;

public class FutureConverter {

    /**
     * Converts {@link ListenableFuture} to  {@link Single}.
     * The original future is canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(ListenableFuture<T> listenableFuture) {
        return RxJavaFutureUtils.createSingle(SpringFutureUtils.createValueSource(listenableFuture));
    }

    /**
     * Converts  {@link Single} to {@link ListenableFuture}.
     */
    public static <T> ListenableFuture<T> toListenableFuture(Single<T> single) {
        return SpringFutureUtils.createListenableFuture(RxJavaFutureUtils.createValueSource(single));
    }

}
