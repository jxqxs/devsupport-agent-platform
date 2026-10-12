package com.example.devsupport.juc;

public class SynchronizedCounterDemo {
    private int count = 0;
    private int times=10000;
    private Object lock = new Object();
    public static void main(String[] args) {
        SynchronizedCounterDemo demo=new SynchronizedCounterDemo();
        demo.test1();
    }
    public void increment() {
        synchronized (lock) {
            count++;
        }

    }
    public void test1(){
        Thread t1=new Thread(()->{
            for (int i = 0; i < times; i++) {
                increment();
            }
        });
        Thread t2=new Thread(()->{
            for (int i = 0; i < times; i++) {
                increment();
            }
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("count = " + count);
        System.out.println("expected count = " + times * 2);
    }


}
