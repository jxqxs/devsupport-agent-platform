package com.example.devsupport.juc;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerDemo {
    private static final AtomicInteger count = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
        int times = 100_000;
        Thread first = new Thread(() -> increment(times));
        Thread second = new Thread(() -> increment(times));
        first.start();
        second.start();
        first.join();
        second.join();
        System.out.println("atomic count = " + count.get() + ", expected " + (times * 2));
    }

    private static void increment(int times) {
        for (int i = 0; i < times; i++) {
            count.incrementAndGet();
        }
    }
}
