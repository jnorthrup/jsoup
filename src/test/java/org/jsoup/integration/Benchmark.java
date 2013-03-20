package org.jsoup.integration;

import java.util.Date;

/**
 Does an A/B org.jsoup.test on two methods, and prints out how long each took.

 @author Jonathan Hedley, jonathan@hedley.net */
final class Benchmark {
    private Benchmark() {
    }

// --Commented out by Inspection START (3/20/13 10:02 AM):
//    public static void run(Runnable a, Runnable b, int count) {
//        long aMillis;
//        long bMillis;
//
//        print("Running org.jsoup.test A (x%d)", count);
//        aMillis = time(a, count);
//        print("Running org.jsoup.test B");
//        bMillis = time(b, count);
//
//        print("\nResults:");
//        print("A: %.2fs", aMillis / 1000.00f);
//        print("B: %.2fs", bMillis / 1000.0f);
//        print("\nB ran in %.2f %% time of A\n", bMillis * 1.0f / aMillis * 1.0f * 100.0f);
//    }
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

    private static long time(Runnable test, int count) {
        Date start = new Date();
        for (int i = 0; i < count; i++) {
            test.run();
        }
        Date end = new Date();
        return end.getTime() - start.getTime();
    }

    private static void print(String msgFormat, Object... msgParams) {
        System.out.println(String.format(msgFormat, msgParams));
    }
}
