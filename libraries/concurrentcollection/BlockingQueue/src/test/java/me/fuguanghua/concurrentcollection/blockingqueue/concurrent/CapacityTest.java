package me.fuguanghua.concurrentcollection.blockingqueue.concurrent;


import org.junit.Assert;
import org.junit.Test;

public class CapacityTest {

    @Test
    public void testOne() {

        Assert.assertEquals(1, Capacity.getCapacity(1));
    }
    
    @Test
    public void testThree() {

        Assert.assertEquals(4, Capacity.getCapacity(3));
    }
    
    @Test
    public void testIntMax() {

        Assert.assertEquals(Capacity.MAX_POWER2, Capacity.getCapacity(Integer.MAX_VALUE));
    }


    @Test
    public void testIntMax2() {

        Assert.assertEquals(Capacity.MAX_POWER2, Capacity.getCapacity(Integer.MAX_VALUE/2));
    }

    @Test
    public void testIntMax2Plus1() {

        Assert.assertEquals(Capacity.MAX_POWER2, Capacity.getCapacity(Integer.MAX_VALUE/2+1));
    }
}
