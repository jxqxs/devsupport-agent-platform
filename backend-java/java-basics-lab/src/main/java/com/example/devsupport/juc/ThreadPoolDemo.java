package com.example.devsupport.juc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 核心线程先接任务，接满后进入有界队列，队列满了再把线程加到最大数，还放不下就拒绝。
 */
public class ThreadPoolDemo {
    private static final int CORE_POOL_SIZE = 4;//核心线程数
    private static final int MAXIMUM_POOL_SIZE = 8;//最大线程数
    private static final int QUEUE_CAPACITY = 10;//队列容量
    private static final long KEEP_ALIVE_TIME = 1L;//线程存活时间

    public static void main(String[] args) throws InterruptedException {
        test1();
    }

    public static void test1() throws InterruptedException {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.AbortPolicy());

        int rejected = 0;
        for (int i = 0; i < 50; i++) {
            try {
                pool.submit(ThreadPoolDemo::run);
            } catch (RejectedExecutionException e) {
                rejected++;
            }
        }
        System.out.println("poolSize=" + pool.getPoolSize()
                + ", queueSize=" + pool.getQueue().size()
                + ", rejected=" + rejected);
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
    }

    public static void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " is running");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
