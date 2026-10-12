package com.example.devsupport.juc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RequestIdDemo {
    private static final ThreadLocal<String> requestId = new ThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (String id : new String[] {"req-alice", "req-bob"}) {
            pool.submit(() -> setRequestId(id));
        }
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void setRequestId(String id) {
        requestId.set(id);
        try {
            System.out.println(Thread.currentThread().getName() + " requestId = " + requestId.get());
        } finally {
            requestId.remove();
        }
    }
}
