package com.example.devsupport.juc;

public class ArthasDemo {
    private static int total = 0;
    public static void main(String[] args) {
        test1();
    }
    public static void test1() {
        while (true) {
            total++;
            System.out.println("test1, 总共调用了" + total + "次");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
