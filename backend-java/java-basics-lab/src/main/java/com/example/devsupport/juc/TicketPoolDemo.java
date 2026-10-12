package com.example.devsupport.juc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.devsupport.model.Ticket;

/** 100 个工单交给线程池，每个睡眠 100ms，用 CountDownLatch 等全部完成。 */
public class TicketPoolDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("test1(4, 8, 100)");
        test1(4, 8, 100);
        System.out.println("test1(2, 4, 2)");
        test1(2, 4, 2);
    }
    public static void test1(int core, int max, int queueSize) {
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            tickets.add(new Ticket("id" + i, "title" + i, "description" + i, "status" + i, "priority" + i, "source" + i, "createdAt" + i, "updatedAt" + i));
        }

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                core,
                max,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        CountDownLatch latch = new CountDownLatch(tickets.size());
        AtomicInteger callerRuns = new AtomicInteger();

        long start = System.currentTimeMillis();
        for (Ticket ticket : tickets) {
            executor.submit(() -> {
                try {
                    Thread.sleep(100);
                    String threadName = Thread.currentThread().getName();
                    if ("main".equals(threadName)) {
                        callerRuns.incrementAndGet();//如果当前线程是main线程,则计数器加1
                        System.out.println(threadName + " " + ticket.getId());
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long elapsed = System.currentTimeMillis() - start;
        executor.shutdown();
        System.out.println("pool tickets=" + tickets.size()
                + ", callerRuns=" + callerRuns.get()
                + ", elapsed=" + elapsed + "ms");
    }
}
