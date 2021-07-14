package me.fuguanghua.concurrentcollection.blockingqueue.concurrent;

/*
 * #%L
 * Conversant Disruptor
 * ~~
 * Conversantmedia.com © 2016, Conversant, Inc. Conversant® is a trademark of Conversant, Inc.
 * ~~
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.conversantmedia.util.concurrent.ConcurrentQueue;
import org.junit.Ignore;
import com.conversantmedia.util.estimation.Percentile;

/**
 */
public class ConcurrentQueuePerformanceTest {

    // increase this number for a legit performance test
    static final int NRUN = 16*1*1024;
    static final int NTHREAD = 32;

    private static final Integer INTVAL = 173;
    private static final long[] offerPctTimes = new long[NRUN];
    private static final long[] pollPctTimes = new long[NRUN];
    private static final long[] totPctTimes = new long[NRUN];

    private static final Integer[] first1024 = new Integer[1024];

    static {
        for(int i=0; i<1024; i++) {
            first1024[i] = i;
        }
    }

    @Ignore
    public static void testPerformance(final ConcurrentQueue<Integer> rb) throws InterruptedException, Percentile.InsufficientSamplesException {
        for(int c=0; c<3; c++) {
            System.gc();
            runPerformance(rb);
        }
    }

    @Ignore
    public static void runPerformance(final ConcurrentQueue<Integer> rb) throws InterruptedException, Percentile.InsufficientSamplesException {

        final int size = 1024;
        final long mask = size-1;

        final Percentile offerPct = new Percentile();
        final Percentile pollPct = new Percentile();
        final Percentile totPct = new Percentile();

        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                int i = NRUN;
                do {
                    final long startTime = System.nanoTime();

                    while (!rb.offer(first1024[(int) (startTime>>10 & mask)])) {
                        Thread.yield();
                    }
                    offerPctTimes[i-1] = System.nanoTime()-startTime;
                } while (i-- > 1);

                for(int d=0; d<offerPctTimes.length; d++) {
                    offerPct.add(offerPctTimes[d] / 1e3F);
                }

            }
        });

        thread.start();
        Integer result;
        int i = NRUN;

        do {
            final long startTime = System.nanoTime();

            while ((result = rb.poll()) == null) {
                Thread.yield();
            }

            final int diff = (int) (System.nanoTime()>>10 & mask) - result.intValue();

            totPctTimes[i-1] = diff;

            pollPctTimes[i-1] = System.nanoTime() - startTime;
        } while (i-- > 1);

        for(int d=0; d<pollPctTimes.length; d++) {
            pollPct.add(pollPctTimes[d] / 1e3F);
        }

        for(int d=0; d<totPctTimes.length; d++) {
            if(totPctTimes[d] > 0) {
                totPct.add(totPctTimes[d]);
            }
        }

        thread.join();

        Percentile.print(System.out, "offer (us):", offerPct);
        Percentile.print(System.out, "poll (us): ", pollPct);
        Percentile.print(System.out, "tot (~us): ", totPct);
    }

    @Ignore
    public static void testRate(final ConcurrentQueue<Integer> rb) throws InterruptedException, Percentile.InsufficientSamplesException {
        for(int c=0; c<5; c++) {
            System.gc();
            runRate(rb);
        }
    }

    @Ignore
    public static void runRate(final ConcurrentQueue<Integer> rb) throws InterruptedException, Percentile.InsufficientSamplesException {
        final int size = 1024;


        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                int i = NRUN;
                do {
                    while (!rb.offer(INTVAL)) {
                        Thread.yield();
                    }
                } while (i-- != 0);
            }
        });


        final long startTime = System.nanoTime();

        thread.start();
        Integer result;
        int i = NRUN;
        do {
            while ((result = rb.poll()) == null) {
                Thread.yield();
            }

        } while (i-- != 0);

        thread.join();
        final long runTime = System.nanoTime() - startTime;
        System.out.println(Integer.toString(NRUN)+" in "+String.format("%3.1f ms", runTime/1e6)+": "+String.format("%d ns", runTime/NRUN));
    }


    @Ignore
    public static void testNThreadPerformance(final ConcurrentQueue<Integer> rb) throws InterruptedException, Percentile.InsufficientSamplesException {
        for(int c=0; c<3; c++) {
            System.gc();
            runNThreadPerformance(rb);
        }
    }

    @Ignore
    public static void runNThreadPerformance(final ConcurrentQueue<Integer> rb) throws InterruptedException, Percentile.InsufficientSamplesException {

        final int size = 1024;
        final long mask = size-1;

        final Percentile offerPct = new Percentile();
        final Percentile pollPct = new Percentile();
        final Percentile totPct = new Percentile();

        final Thread[] thread = new Thread[NTHREAD];

        for(int t = 0; t<NTHREAD; t++) {
            thread[t] = new Thread(new Runnable() {

                @Override
                public void run() {
                    int i = NRUN/NTHREAD;
                    do {
                        final long startTime = System.nanoTime();

                        while(!rb.offer(first1024[(int) (startTime>>10 & mask)])) {
                            Thread.yield();
                        }
                        offerPctTimes[i-1] = System.nanoTime()-startTime;
                    } while(i-->1);

                    for(int d = 0; d<offerPctTimes.length; d++) {
                        offerPct.add(offerPctTimes[d]/1e3F);
                    }

                }
            });
            thread[t].start();
        }


        Integer result;
        int i = NRUN;

        do {
            final long startTime = System.nanoTime();

            while ((result = rb.poll()) == null) {
                Thread.yield();
            }

            final int diff = (int) (System.nanoTime()>>10 & mask) - result.intValue();

            totPctTimes[i-1] = diff;

            pollPctTimes[i-1] = System.nanoTime() - startTime;
        } while (i-- > 1);

        for(int d=0; d<pollPctTimes.length; d++) {
            pollPct.add(pollPctTimes[d] / 1e3F);
        }

        for(int d=0; d<totPctTimes.length; d++) {
            if(totPctTimes[d] > 0) {
                totPct.add(totPctTimes[d]);
            }
        }

        Percentile.print(System.out, "offer (us):", offerPct);
        Percentile.print(System.out, "poll (us): ", pollPct);
        Percentile.print(System.out, "tot (~us): ", totPct);

        for(int t=0; t<NTHREAD; t++) {
            thread[t].join();
        }
    }

    @Ignore
    public static void testNxNThreadPerformance(final ConcurrentQueue<Integer> rb) throws InterruptedException, Percentile.InsufficientSamplesException {
        for(int c=0; c<3; c++) {
            System.gc();
            runNxNThreadPerformance(rb);
        }
    }

    @Ignore
    public static void runNxNThreadPerformance(final ConcurrentQueue<Integer> rb) throws InterruptedException, Percentile.InsufficientSamplesException {

        final int size = 1024;
        final long mask = size-1;

        final Percentile offerPct = new Percentile();
        final Percentile pollPct = new Percentile();
        final Percentile totPct = new Percentile();

        final Thread[] thread = new Thread[NTHREAD*2];

        for(int t = 0; t<NTHREAD; t++) {
            thread[t] = new Thread(() -> {
                final int blockSize = NRUN/NTHREAD;;
                final long[] offerTimes = new long[blockSize];
                int i = blockSize;
                do {
                    final long startTime = System.nanoTime();

                    while(!rb.offer(first1024[(int) (startTime>>10 & mask)])) {
                        Thread.yield();
                    }
                    offerTimes[i-1] = System.nanoTime()-startTime;
                } while(i-->1);

                synchronized (offerPct) {
                    for (int d = 0; d < offerTimes.length; d++) {
                        final float time = offerTimes[d] / 1e3F;
                        if(time < 1F) {
                            offerPct.add(time);
                        }
                    }
                }

            });
            thread[t].start();
        }


        for(int t=NTHREAD; t<2*NTHREAD; t++) {
            thread[t] = new Thread(() -> {
                Integer result;
                final int blockSize = NRUN/NTHREAD;
                int i = blockSize;
                final long[] pollTimes = new long[blockSize];
                final long[] totTimes  = new long[blockSize];

                do {
                    final long startTime = System.nanoTime();

                    while ((result = rb.poll()) == null) {
                        Thread.yield();
                    }

                    final int diff = (int) (System.nanoTime() >> 10 & mask) - result.intValue();

                    totTimes[i - 1] = diff;

                    pollTimes[i - 1] = System.nanoTime() - startTime;
                } while (i-- > 1);

                synchronized (pollPct) {
                    for (int d = 0; d < pollTimes.length; d++) {
                        final float time = pollTimes[d] / 1e3F;
                        if(time < 1F) {
                            pollPct.add(time);
                        }
                    }
                }

                synchronized (totPct) {
                    for (int d = 0; d < totTimes.length; d++) {
                        if (totTimes[d] > 0F && totTimes[d] < 100.0) {
                            totPct.add(totTimes[d]);
                        }
                    }
                }
            });
            thread[t].start();
        }

        for(int t=0; t<NTHREAD; t++) {
            thread[t].join();
        }

        Percentile.print(System.out, "offer (us):", offerPct);
        Percentile.print(System.out, "poll (us): ", pollPct);
        Percentile.print(System.out, "tot (~us): ", totPct);
    }


}
