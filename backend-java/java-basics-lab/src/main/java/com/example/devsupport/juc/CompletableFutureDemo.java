package com.example.devsupport.juc;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureDemo {
    public static void main(String[] args) {
        test2();
        test1();
    }

    public static String searchDockerDocs() {
        System.out.println("searchDockerDocs start :" + Thread.currentThread().getName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("searchDockerDocs end :" + Thread.currentThread().getName());
        return "docker docs";
    }

    public static String searchGithubIssues() {
        System.out.println("searchGithubIssues start :" + Thread.currentThread().getName());
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("searchGithubIssues end :" + Thread.currentThread().getName());
        return "github issues";
    }

    /** 两个查询同时提交，allOf 等到都结束。 */
    public static void test1() {
        long start = System.currentTimeMillis();
        CompletableFuture<String> docs = CompletableFuture.supplyAsync(() -> searchDockerDocs());
        CompletableFuture<String> issues = CompletableFuture.supplyAsync(() -> searchGithubIssues());
        CompletableFuture.allOf(docs, issues).join();
        long end = System.currentTimeMillis();
        System.out.println("parallel time :" + (end - start) + "ms");
    }

    /** 主线程里一个接一个查。 */
    public static void test2() {
        long start = System.currentTimeMillis();
        searchDockerDocs();
        searchGithubIssues();
        long end = System.currentTimeMillis();
        System.out.println("serial time :" + (end - start) + "ms");
    }
}
