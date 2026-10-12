package com.example.devsupport.juc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 每个线程有自己的一份值。用完要 remove，避免线程复用时串到下一个任务。 */
public class ThreadLocalDemo {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {
        Thread alice = new Thread(() -> printUser("alice"), "alice-thread");//
        Thread bob = new Thread(() -> printUser("bob"), "bob-thread");
        alice.start();
        bob.start();
        alice.join();
        bob.join();

        reuseWithoutRemove();
        twoThreadsWithoutRemove();
    }

    /** 同一条池线程先写入 alice，不 remove，下一个任务再读。 */
    private static void reuseWithoutRemove() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.submit(() -> {
            currentUser.set("alice");
            System.out.println(Thread.currentThread().getName() + " task1 sees " + currentUser.get());
        }).get();
        pool.submit(() -> {
            System.out.println(Thread.currentThread().getName() + " task2 sees " + currentUser.get());
        }).get();
        pool.shutdown();
    }

    /** 池里两条线程各写各的值，都不 remove。下一批任务只读到自己那条线程留下的值。 */
    private static void twoThreadsWithoutRemove() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch started = new CountDownLatch(2);//等待两条线程都启动，2是表示等待两条线程都启动
        CountDownLatch release = new CountDownLatch(1);//等待两条线程都执行完,1是表示等待一条线程执行完，为啥是1，因为后面要释放，释放后才能读到值
        for (String name : new String[] {"alice", "bob"}) {
            pool.submit(() -> {
                currentUser.set(name);
                System.out.println(Thread.currentThread().getName() + " set " + currentUser.get());
                started.countDown();
                release.await();
                return null;
            });
        }
        started.await();
        var laterAlice = pool.submit(() -> Thread.currentThread().getName() + " later sees " + currentUser.get());//返回的是Future对象，可以调用get方法获取结果
        var laterBob = pool.submit(() -> Thread.currentThread().getName() + " later sees " + currentUser.get());//返回的是Future对象，可以调用get方法获取结果
        release.countDown();
        System.out.println(laterAlice.get());
        System.out.println(laterBob.get());
        pool.shutdown();
    }

    private static void printUser(String username) {
        currentUser.set(username);
        try {
            System.out.println(Thread.currentThread().getName() + " sees " + currentUser.get());
        } finally {
            currentUser.remove();
        }
    }
}
