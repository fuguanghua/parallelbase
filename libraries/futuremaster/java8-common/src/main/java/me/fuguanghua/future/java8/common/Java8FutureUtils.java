package me.fuguanghua.future.java8.common;

import me.fuguanghua.parallelbase.future.common.ValueSource;
import me.fuguanghua.parallelbase.future.common.ValueSourceFuture;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Java8FutureUtils {
    public static <T> CompletableFuture<T> createCompletableFuture(ValueSource<T> valueSource) {
        if (valueSource instanceof CompletableFuturebackedValueSource) {
            return ((CompletableFuturebackedValueSource<T>) valueSource).getWrappedFuture();
        } else {
            return new ValueSourcebackedCompletableFuture<T>(valueSource);
        }
    }

    public static <T> ValueSourceFuture<T> createValueSourceFuture(CompletableFuture<T> completableFuture) {
        if (completableFuture instanceof ValueSourcebackedCompletableFuture &&
                ((ValueSourcebackedCompletableFuture<T>) completableFuture).getValueSource() instanceof ValueSourceFuture) {
            return (ValueSourceFuture<T>) ((ValueSourcebackedCompletableFuture<T>) completableFuture).getValueSource();
        } else {
            return new CompletableFuturebackedValueSource<>(completableFuture);
        }
    }

    public static <T> ValueSource<T> createValueSource(CompletableFuture<T> completableFuture) {
        if (completableFuture instanceof ValueSourcebackedCompletableFuture) {
            return ((ValueSourcebackedCompletableFuture<T>) completableFuture).getValueSource();
        } else {
            return new CompletableFuturebackedValueSource<>(completableFuture);
        }
    }

    /**
     * CompletableFuture that takes values from the ValueSource. CompletableFuture is a class, not
     * an interface so we can not just forward events from the ValueSource, we to always instantiate the class.
     */
    private static final class ValueSourcebackedCompletableFuture<T> extends CompletableFuture<T> {
        private final ValueSource<T> valueSource;

        private ValueSourcebackedCompletableFuture(ValueSource<T> valueSource) {
            this.valueSource = valueSource;
            valueSource.addCallbacks(this::complete, this::completeExceptionally);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (isDone()) {
                return false;
            }
            boolean result = valueSource.cancel(mayInterruptIfRunning);
            super.cancel(mayInterruptIfRunning);
            return result;
        }

        private ValueSource<T> getValueSource() {
            return valueSource;
        }
    }

    private static final class CompletableFuturebackedValueSource<T> extends ValueSourceFuture<T> {
        private CompletableFuturebackedValueSource(CompletableFuture<T> completableFuture) {
            super(completableFuture);
        }


        @Override
        public void addCallbacks(Consumer<T> successCallback, Consumer<Throwable> failureCallback) {
            getWrappedFuture().whenComplete((v, t) -> {
                if (t == null) {
                    successCallback.accept(v);
                } else {
                    failureCallback.accept(t);
                }
            });
        }

        @Override
        protected CompletableFuture<T> getWrappedFuture() {
            return (CompletableFuture<T>) super.getWrappedFuture();
        }
    }
}
