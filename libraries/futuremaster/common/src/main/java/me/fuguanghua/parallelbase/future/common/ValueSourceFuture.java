package me.fuguanghua.parallelbase.future.common;

import java.util.concurrent.Future;

/**
 * Some ValueSources are already futures, let's wrap it and use its implementation.
 */
public abstract class ValueSourceFuture<T> extends FutureWrapper<T> implements ValueSource<T> {
    protected ValueSourceFuture(Future<T> wrappedFuture) {
        super(wrappedFuture);
    }
}
