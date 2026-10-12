package com.example.devsupport.juc;

import java.util.ArrayList;
import java.util.List;

//不断创建大数组，导致OOM,用小堆运行程序，可以看到内存占用不断增加，直到OOM
public class OomDemo {
    public static void main(String[] args) {
        test1();
    }
    public static void test1() {
        List<byte[]> holder = new ArrayList<>();
        int total = 0;
        while (true) {
            try {
            holder.add(new byte[1024 * 1024]);
            total++;
            System.out.println("创建了 1MB，总共 " + total + "MB");
        } 
        catch (OutOfMemoryError e) {
            holder.clear(); 
            System.out.println("内存不足");
            break;
        }
    }
}
 }

