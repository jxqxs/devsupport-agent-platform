package com.example.devsupport.juc;
/*主要是做锁的demo，ReentrantLock是可重入锁，可以用来实现互斥锁和读写锁，
实现方式是基于AQS（AbstractQueuedSynchronizer）实现的
代码实现如下：
public class ReentrantLockDemo {
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
}*/
import java.util.concurrent.locks.ReentrantLock;
public class ReentrantLockDemo {
private int times = 100_000;
private final ReentrantLock lock = new ReentrantLock();
private int count=0;

public void increment(int times) {
    for (int i = 0; i < times; i++) {
    lock.lock();
    try {
        count++;
    } finally {
        lock.unlock();
    }
}
}
public  void test1() throws InterruptedException{
    Thread first = new Thread(() -> increment(times));
    Thread second = new Thread(() -> increment(times));
    first.start();
    second.start();
    first.join();
    second.join();
    System.out.println("ReentrantLockDemo:  count = " + count + ", expected " + (times * 2));
}

public static void main(String[] args) throws InterruptedException{
    ReentrantLockDemo reentrantLockDemo = new ReentrantLockDemo();
    reentrantLockDemo.test1();
}
}
