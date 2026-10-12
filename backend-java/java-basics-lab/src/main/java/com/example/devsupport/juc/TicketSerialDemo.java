package com.example.devsupport.juc;

import java.util.ArrayList;
import java.util.List;

import com.example.devsupport.model.Ticket;

/** 100 个工单串行处理，每个睡眠 100ms，总耗时大约 10 秒。 */
public class TicketSerialDemo {
    public static void main(String[] args) {
        test1();
    }

    public static void test1() {
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            tickets.add(new Ticket("id" + i, "title" + i, "description" + i, "status" + i, "priority" + i, "source" + i, "createdAt" + i, "updatedAt" + i));
        }

        long start = System.currentTimeMillis();
        for (Ticket ticket : tickets) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            System.out.println(ticket.getId());
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("serial tickets=" + tickets.size() + ", elapsed=" + elapsed + "ms");
    }
}
