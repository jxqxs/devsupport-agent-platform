package com.example.devsupport.juc;

/** 两个线程同时对普通 int 做 count++，结果会小于预期。 */
public class CounterDemo {
    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        int times = 100_000;
        Thread first = new Thread(() -> increment(times));
        Thread second = new Thread(() -> increment(times));
        first.start();
        second.start();
        first.join();
        second.join();
        System.out.println("unsafe count = " + count + ", expected " + (times * 2));
    }

    private static void increment(int times) {
        for (int i = 0; i < times; i++) {
            count++;
        }
    }
}
