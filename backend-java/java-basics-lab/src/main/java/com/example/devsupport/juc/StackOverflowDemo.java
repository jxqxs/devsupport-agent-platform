package com.example.devsupport.juc;

public class StackOverflowDemo {
    private static int total = 0;
    public static void main(String[] args) {

    try {
        test1();
    } catch (StackOverflowError e) {
        System.out.println("栈溢出, 总共调用了" + total + "次");
    }
}

public static void test1() {
    total++;
    test1();
}
}

