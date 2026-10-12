package com.example.devsupport.juc;

import java.util.concurrent.locks.ReentrantLock;

public class TryLockDemo {
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;

    public static void main(String[] args) throws InterruptedException {
        TryLockDemo tryLockDemo = new TryLockDemo();
        tryLockDemo.test1();
        System.out.println("count = " + tryLockDemo.count);
    }

    /** 先占住锁一小段时间，让另一条线程碰到已经锁住的临界区。 */
    public void hold() {
        lock.lock();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void increment() {
        if (lock.tryLock()) {
            try {
                count++;
                System.out.println(Thread.currentThread().getName() + " got lock, count = " + count);
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println(Thread.currentThread().getName() + " did not get lock");
        }
    }

    public void test1() throws InterruptedException {
        Thread holder = new Thread(this::hold, "holder");
        Thread worker = new Thread(this::increment, "worker");
        holder.start();
        Thread.sleep(50);
        worker.start();
        holder.join();
        worker.join();
    }
}
