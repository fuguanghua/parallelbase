package me.fuguanghua.concurrentcollection.blockingqueue.concurrent;

import me.fuguanghua.concurrentcollection.blockingqueue.collection.Stack;

import java.util.concurrent.TimeUnit;

public interface BlockingStack<N> extends Stack<N> {

    /**
     * Push an element on the stack, waiting if necessary if the stack is currently full
     *
     * @param n - the element to push on the stack
     * @param time - the maximum time to wait
     * @param unit - unit of waiting time
     * @return boolean - true if item was pushed, false otherwise
     *
     * @throws InterruptedException on interrupt
     */
    boolean push(final N n, final long time, final TimeUnit unit) throws InterruptedException;

    /**
     * Push an element on the stack waiting as long as required for space to become available
     *
     * @param n - the element to push
     * @throws InterruptedException - in the event the current thread is interrupted prior to pushing the element
     */
    void pushInterruptibly(final N n) throws InterruptedException;

    /**
     * Pop an element from the stack, waiting if necessary if the stack is currently empty
     *
     * @param time - the maximum time to wait
     * @param unit - the time unit for the waiting time
     * @return N - the popped element, or null in the event of a timeout
     *
     * @throws InterruptedException on interrupt
     */
    N pop(final long time, final TimeUnit unit) throws InterruptedException;

    /**
     * Pop an element from the stack, waiting as long as required for an element to become available on the
     * stack
     *
     * @return N - the popped element
     * @throws InterruptedException - in the event the current thread is interrupted prior to popping any element
     */
    N popInterruptibly() throws InterruptedException;

}
