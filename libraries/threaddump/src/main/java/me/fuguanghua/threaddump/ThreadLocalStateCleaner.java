package me.fuguanghua.threaddump;

import org.apache.commons.lang3.ThreadUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Cleans up thread local state for all threads for a given thread local instance.
 */
public final class ThreadLocalStateCleaner {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadLocalStateCleaner.class);
    public static final ThreadLocalStateCleaner INSTANCE = new ThreadLocalStateCleaner();
    private static final Method GET_THREADLOCAL_MAP_METHOD = MethodUtils
            .getMatchingMethod(ThreadLocal.class, "getMap", Thread.class);

    static {
        GET_THREADLOCAL_MAP_METHOD.setAccessible(true);
    }

    private volatile Method removeThreadlocalMethod;
    private volatile Method getThreadlocalEntryMethod;
    private volatile Field threadLocalEntryValueField;

    // enforce singleton
    private ThreadLocalStateCleaner() {

    }

    // use reflection to clear the state of the given thread local and thread
    public <T> void cleanupThreadLocal(ThreadLocal<?> threadLocal, Thread thread,
                                       BiConsumer<Thread, T> cleanedValueListener) {
        Objects.nonNull(threadLocal);
        Objects.nonNull(thread);
        try {
            Object threadLocalMap = GET_THREADLOCAL_MAP_METHOD.invoke(threadLocal, thread);
            if (threadLocalMap != null) {
                if (cleanedValueListener != null) {
                    callCleanedValueListener(threadLocal, thread, cleanedValueListener, threadLocalMap);
                }
                if (removeThreadlocalMethod == null) {
                    removeThreadlocalMethod = MethodUtils.getMatchingMethod(
                            threadLocalMap.getClass(), "remove", ThreadLocal.class);
                    removeThreadlocalMethod.setAccessible(true);
                }
                removeThreadlocalMethod.invoke(threadLocalMap, threadLocal);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.warn("Cannot cleanup thread local", e);
        }
    }

    private <T> void callCleanedValueListener(ThreadLocal<?> threadLocal, Thread thread,
                                              BiConsumer<Thread, T> cleanedValueListener, Object threadLocalMap)
            throws IllegalAccessException, InvocationTargetException {
        T currentValue = getCurrentValue(threadLocal, threadLocalMap);
        if (currentValue != null) {
            cleanedValueListener.accept(thread, currentValue);
        }
    }

    public <T> T getThreadLocalValue(ThreadLocal<?> threadLocal, Thread thread)
            throws InvocationTargetException, IllegalAccessException {
        Objects.nonNull(threadLocal);
        Objects.nonNull(thread);
        Object threadLocalMap = GET_THREADLOCAL_MAP_METHOD.invoke(threadLocal, thread);
        if (threadLocalMap != null) {
            return getCurrentValue(threadLocal, threadLocalMap);
        } else {
            return null;
        }
    }

    private <T> T getCurrentValue(ThreadLocal<?> threadLocal, Object threadLocalMap) throws IllegalAccessException,
            InvocationTargetException {
        if (getThreadlocalEntryMethod == null) {
            getThreadlocalEntryMethod = MethodUtils.getMatchingMethod(
                    threadLocalMap.getClass(), "getEntry", ThreadLocal.class);
            getThreadlocalEntryMethod.setAccessible(true);
        }
        Object entry = getThreadlocalEntryMethod.invoke(threadLocalMap, threadLocal);
        if (entry != null) {
            if (threadLocalEntryValueField == null) {
                threadLocalEntryValueField = FieldUtils.getField(entry.getClass(), "value",
                        true);
            }
            return (T) threadLocalEntryValueField.get(entry);
        }
        return null;
    }

    // cleanup thread local state on all active threads
    public <T> void cleanupThreadLocal(ThreadLocal<?> threadLocal, BiConsumer<Thread, T> cleanedValueListener) {
        Objects.nonNull(threadLocal);
        for (Thread thread : ThreadUtils.getAllThreads()) {
            cleanupThreadLocal(threadLocal, thread, cleanedValueListener);
        }
    }
}
