package me.fuguanghua.concurrentcollection.blockingqueue.concurrent;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import com.conversantmedia.util.concurrent.SpinPolicy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 */
public class DisruptorFairSchedulingTest {

    private static final int NTHREAD = 4*Runtime.getRuntime().availableProcessors();

    private static final Long LONGMSG = Long.valueOf(3663L);
    public static final long TIMEOUT = 200_000L;
    public static final int MIN_PROGRESS = 0;
    public static final long JOIN_TIMEOUT = 2000L;
    public static final int QUEUE_SZ = 1024;

    private BlockingQueue<Long> msgQueue;

    private volatile boolean isRunning;


    @Before
    public void setup() {
        msgQueue = new DisruptorBlockingQueue<Long>(QUEUE_SZ, SpinPolicy.WAITING);
        isRunning = true;
    }

    @Test(timeout=TIMEOUT)
    public void testPutTakeProgress() throws InterruptedException {
        final TakeProgressCheck[] check = new TakeProgressCheck[NTHREAD];
        final Thread[] thread = new Thread[NTHREAD];
        for(int i=0; i<NTHREAD; i++) {
            check[i] = new TakeProgressCheck();
            thread[i] = new Thread(check[i], "Check "+(i+1));
            thread[i].start();
        }

        final long startTime = System.currentTimeMillis();

        while(isRunning) {
            boolean failedProgress = false;
            for(int i=0; i<NTHREAD; i++) {
                msgQueue.put(LONGMSG);
                if(check[i].madeProgress == -1) {
                    failedProgress = true;
                }
            }
            isRunning = failedProgress && System.currentTimeMillis()-startTime < TIMEOUT/2;
        }

        boolean allProgressed = true;
        for(int i=0; i<NTHREAD; i++) {
            if(check[i].madeProgress < MIN_PROGRESS) {
                allProgressed = false;
            }
            System.out.print(check[i].madeProgress);
            System.out.print(' ');
        }

        System.out.println();

        for(int i=0; i<NTHREAD; i++) {
            // many threads will be waiting for take to return
            msgQueue.put(LONGMSG);
        }

        for(int i=0; i<NTHREAD; i++) {
            thread[i].join(JOIN_TIMEOUT);
        }

        Assert.assertTrue(allProgressed);


    }

    @Test(timeout= TIMEOUT)
    public void testOfferPollProgress() throws InterruptedException {
        final PollProgressCheck[] check = new PollProgressCheck[NTHREAD];
        final Thread[] thread = new Thread[NTHREAD];
        for(int i=0; i<NTHREAD; i++) {
            check[i] = new PollProgressCheck();
            thread[i] = new Thread(check[i], "Check "+(i+1));
            thread[i].start();
        }

        final long startTime = System.currentTimeMillis();

        while(isRunning) {
            boolean failedProgress = false;
            for(int i=0; i<NTHREAD; i++) {
                msgQueue.offer(LONGMSG);
                if(check[i].madeProgress == -1) {
                    failedProgress = true;
                }
            }
            isRunning = failedProgress && System.currentTimeMillis()-startTime < TIMEOUT/2;
        }

        boolean allProgressed = true;
        for(int i=0; i<NTHREAD; i++) {
            if(check[i].madeProgress < MIN_PROGRESS) {
                allProgressed = false;
            }
            System.out.print(check[i].madeProgress);
            System.out.print(' ');
        }

        System.out.println();

        for(int i=0; i<NTHREAD; i++) {
            thread[i].join(JOIN_TIMEOUT);
        }

        Assert.assertTrue(allProgressed);


    }




    private class TakeProgressCheck implements Runnable {
        volatile long madeProgress = -1;

        @Override
        public void run() {
            while(isRunning) {
                final Long v;
                try {
                    v = msgQueue.take();
                    if(v == LONGMSG) {
                        madeProgress++;
                    }

                } catch (InterruptedException e) {
                    isRunning = false;
                }
            }
        }
    }

    private class PollProgressCheck implements Runnable {
        long madeProgress = -1;

        @Override
        public void run() {
            while(isRunning) {
                final Long v;
                try {
                    v = msgQueue.poll(100L, TimeUnit.MILLISECONDS);
                    if(v == LONGMSG) {
                        madeProgress++;
                    }

                } catch (InterruptedException e) {
                    isRunning = false;
                }
            }
        }
    }

}
