package me.fuguanghua.parallelbase.apifuture;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import me.fuguanghua.parallelbase.future.common.FutureWrapper;
import me.fuguanghua.parallelbase.future.common.ValueSource;
import me.fuguanghua.parallelbase.future.common.ValueSourceFuture;

import java.util.concurrent.Executor;
import java.util.function.Consumer;


public class ApiFutureUtils {
    // *************************************** Converting to ApiFuture ******************************************

    /**
     * Creates api future from ValueSourceFuture. We have to send all Future API calls to ValueSourceFuture.
     */
    public static <T> ApiFuture<T> createApiFuture(ValueSourceFuture<T> valueSourceFuture) {
        if (valueSourceFuture instanceof ApiFutureBackedValueSourceFuture) {
            return ((ApiFutureBackedValueSourceFuture<T>) valueSourceFuture).getWrappedFuture();
        } else {
            return new ValueSourceFutureBackedApiFuture<>(valueSourceFuture);
        }
    }

    public static <T> ApiFuture<T> createApiFuture(ValueSource<T> valueSource) {
        if (valueSource instanceof ApiFutureBackedValueSourceFuture) {
            return ((ApiFutureBackedValueSourceFuture<T>) valueSource).getWrappedFuture();
        } else {
            return new ValueSourceBackedApiFuture<>(valueSource);
        }
    }

    /**
     * If we have ValueSourceFuture, we can use it as the implementation and this class only converts
     * listener registration.
     */
    private static class ValueSourceFutureBackedApiFuture<T> extends FutureWrapper<T> implements ApiFuture<T> {
        ValueSourceFutureBackedApiFuture(ValueSourceFuture<T> valueSourceFuture) {
            super(valueSourceFuture);
        }

        @Override
        protected ValueSourceFuture<T> getWrappedFuture() {
            return (ValueSourceFuture<T>) super.getWrappedFuture();
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            getWrappedFuture().addCallbacks(value -> executor.execute(listener), ex -> executor.execute(listener));
        }
    }


    /**
     * If we only get ValueSource we have to create a ValueSourceFuture. Here we wrap Guavas SettableFuture
     * and use it for listener handling and value storage.
     */
    private static class ValueSourceBackedApiFuture<T> extends FutureWrapper<T> implements ApiFuture<T> {
        private final ValueSource<T> valueSource;

        private ValueSourceBackedApiFuture(ValueSource<T> valueSource) {
            super(com.google.common.util.concurrent.SettableFuture.create());
            this.valueSource = valueSource;
            valueSource.addCallbacks(value -> getWrappedFuture().set(value), ex -> getWrappedFuture().setException(ex));
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            getWrappedFuture().addListener(listener, executor);
        }

        @Override
        protected com.google.common.util.concurrent.SettableFuture<T> getWrappedFuture() {
            return (com.google.common.util.concurrent.SettableFuture<T>) super.getWrappedFuture();
        }


        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            valueSource.cancel(mayInterruptIfRunning);
            return super.cancel(mayInterruptIfRunning);
        }

        private ValueSource<T> getValueSource() {
            return valueSource;
        }
    }


    // *************************************** Converting from ApiFuture ******************************************
    public static <T> ValueSourceFuture<T> createValueSourceFuture(ApiFuture<T> apiFuture) {
        if (apiFuture instanceof ValueSourceFutureBackedApiFuture) {
            return ((ValueSourceFutureBackedApiFuture<T>) apiFuture).getWrappedFuture();
        } else {
            return new ApiFutureBackedValueSourceFuture<>(apiFuture);
        }
    }

    public static <T> ValueSource<T> createValueSource(ApiFuture<T> apiFuture) {
        if (apiFuture instanceof ValueSourceBackedApiFuture) {
            return ((ValueSourceBackedApiFuture<T>) apiFuture).getValueSource();
        } else {
            return new ApiFutureBackedValueSourceFuture<>(apiFuture);
        }
    }

    /**
     * Wraps ApiFuture and exposes it as ValueSourceFuture.
     */
    private static class ApiFutureBackedValueSourceFuture<T> extends ValueSourceFuture<T> {
        private ApiFutureBackedValueSourceFuture(ApiFuture<T> wrappedFuture) {
            super(wrappedFuture);
        }

        @Override
        public void addCallbacks(Consumer<T> successCallback, Consumer<Throwable> failureCallback) {
            ApiFutures.addCallback(getWrappedFuture(), new ApiFutureCallback<T>() {
                @Override
                public void onSuccess(T result) {
                    successCallback.accept(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    failureCallback.accept(t);

                }
            }, MoreExecutors.directExecutor());
        }


        @Override
        protected ApiFuture<T> getWrappedFuture() {
            return (ApiFuture<T>) super.getWrappedFuture();
        }
    }
}
