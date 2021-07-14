package me.fuguanghua.concurrentcollection.blockingqueue.concurrent;

import org.junit.Assert;
import org.junit.Test;

public class ContendedAtomicIntegerTest {

    @Test
    public void testInit() {
        Assert.assertEquals(37347, new ContendedAtomicInteger(37347).get());
    }

    @Test
    public void testSet() {
        final ContendedAtomicInteger ai = new ContendedAtomicInteger(0);
        ai.set(676);
        Assert.assertEquals(676, ai.get());
    }

    @Test
    public void testCAS() {
        final ContendedAtomicInteger ai = new ContendedAtomicInteger(667);

        if(ai.compareAndSet(700, 800)) {
            Assert.fail("CAS operation failed.");
        }

        Assert.assertTrue(ai.compareAndSet(667, 777));
        Assert.assertEquals(777, ai.get());
    }
}
