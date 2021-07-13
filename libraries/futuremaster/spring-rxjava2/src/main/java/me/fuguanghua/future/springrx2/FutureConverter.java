package me.fuguanghua.future.springrx2;

import io.reactivex.Single;
import me.fuguanghua.future.rxjava2common.RxJava2FutureUtils;
import me.fuguanghua.future.springcommon.SpringFutureUtils;
import org.springframework.util.concurrent.ListenableFuture;

public class FutureConverter {

    /**
     * Converts {@link ListenableFuture} to  {@link Single}.
     * The original future is canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(ListenableFuture<T> listenableFuture) {
        return RxJava2FutureUtils.createSingle(SpringFutureUtils.createValueSource(listenableFuture));
    }

    /**
     * Converts  {@link Single} to {@link ListenableFuture}.
     */
    public static <T> ListenableFuture<T> toListenableFuture(Single<T> single) {
        return SpringFutureUtils.createListenableFuture(RxJava2FutureUtils.createValueSource(single));
    }

}
