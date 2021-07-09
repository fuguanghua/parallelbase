package me.fuguanghua.concurrent.utils;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class SemaphoreMapTest {
  @Test
  public void cleanup() throws InterruptedException {
    SemaphoreMap<Integer> ts = new SemaphoreMap<>();
    ts.release(1);
    ts.release(2);
    ts.release(3);
    ts.release(1);

    ts.acquire(1);
    ts.acquire(1);
    ts.acquire(2);
    ts.acquire(3);

    assertEquals(ts.keyCount(), 0);
  }
}
