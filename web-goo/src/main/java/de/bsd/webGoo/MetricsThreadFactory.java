package de.bsd.webGoo;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author John Sanda
 */
public class MetricsThreadFactory implements ThreadFactory, Thread.UncaughtExceptionHandler {


    private AtomicInteger threadNumber = new AtomicInteger(0);

    public Thread newThread(Runnable r) {
        String poolName = "MetricsThreadPool";
        Thread t = new Thread(r, poolName + "-" + threadNumber.getAndIncrement());
        t.setDaemon(false);
        t.setUncaughtExceptionHandler(this);

        return t;
    }

    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Uncaught exception on scheduled thread [{}]" + t.getName() + e);
    }

}
