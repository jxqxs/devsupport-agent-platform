package com.example.devsupport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import com.example.devsupport.model.Ticket;
import com.example.devsupport.exception.BusinessException;
import com.example.devsupport.common.ErrorCode;
import com.example.devsupport.model.KnowledgeSource;
import com.example.devsupport.model.User;
import java.lang.Exception;
import com.example.devsupport.common.Result;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
public class CollectiveLab {
    /* LinkedList 和 ArrayList 随机访问时间对比，使用 ticket 数据进行测试 */
    public static void testLinkedListAndArrayListRandomAccessTime() {
        int size = 100000;
        int times = 10000;
        List<Ticket> tickets = new ArrayList<>();
        List<Ticket> ticketsLinkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            Ticket ticket = new Ticket("id" + i, "title" + i, "description" + i, "status" + i, "priority" + i, "source" + i, "createdAt" + i, "updatedAt" + i);
            tickets.add(ticket);
            ticketsLinkedList.add(ticket);
        }

        Random random = new Random(1);
        int[] indexes = new int[times];
        for (int i = 0; i < times; i++) {
            indexes[i] = random.nextInt(size);
        }

        long startTime = System.currentTimeMillis();
        for (int index : indexes) {
            tickets.get(index).getId();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("ArrayList random access time: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        for (int index : indexes) {
            ticketsLinkedList.get(index).getId();
        }
        endTime = System.currentTimeMillis();
        System.out.println("LinkedList random access time: " + (endTime - startTime) + "ms");
    }
    static class EqualsOnlyKey {
        String id;
    
        EqualsOnlyKey(String id) {
            this.id = id;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EqualsOnlyKey other)) {
                return false;
            }
            return id.equals(other.id);
        }
    }

    static class EqualsHashKey {
        String id;
    
        EqualsHashKey(String id) {
            this.id = id;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EqualsHashKey other)) {
                return false;
            }
            return id.equals(other.id);
        }
    
        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
 public static void testEqualsAndHashcode(){
    Set<EqualsOnlyKey> onlyEquals = new HashSet<>();
    onlyEquals.add(new EqualsOnlyKey("u1"));
    onlyEquals.add(new EqualsOnlyKey("u1"));
    System.out.println(onlyEquals.size()); // 2
    
    Set<EqualsHashKey> both = new HashSet<>();
    both.add(new EqualsHashKey("u1"));
    both.add(new EqualsHashKey("u1"));
    System.out.println(both.size()); // 1

    Map<EqualsHashKey, String> map = new HashMap<>();
EqualsHashKey key = new EqualsHashKey("t1");
map.put(key, "OPEN");
key.id = "t2";
System.out.println(map.get(key));                 // null，改变了key的id，导致map中没有找到对应的key
System.out.println(map.get(new EqualsHashKey("t1"))); // null，创建了新的EqualsHashKey对象，导致map中没有找到对应的key

List<Ticket> tickets = List.of(
    new Ticket("1", "a", "d", "OPEN", "P1", "web", "t", "t"),
    new Ticket("2", "b", "d", "OPEN", "P2", "web", "t", "t"),
    new Ticket("3", "c", "d", "CLOSED", "P1", "web", "t", "t"));

List<Ticket> openTickets = tickets.stream()
    .filter(ticket -> ticket.getStatus().equals("OPEN"))
    .toList();
System.out.println(openTickets.size()); // 2

Map<String, List<Ticket>> grouped = tickets.stream()
    .collect(Collectors.groupingBy(Ticket::getStatus));//collect()方法将stream中的元素收集到一个Map中，key是Ticket的status，value是Ticket的列表
System.out.println(grouped.keySet()); // OPEN, CLOSED，keySet()返回的是一个Set集合，包含了所有的key


 }
 public static Ticket findTicket(Map<String, Ticket> store, String id){
    if(id==null||id.isBlank()){
        throw new BusinessException(ErrorCode.INVALID_PARAMETER);
    }
    Ticket ticket = store.get(id);
    if(ticket==null){
        throw new BusinessException(ErrorCode.TICKET_NOT_FOUND);
    }
    return ticket;

 }

 public static KnowledgeSource findKnowledgeSource(Map<String,KnowledgeSource>store,String id){
    if(id==null||id.isBlank()){
        throw new BusinessException(ErrorCode.INVALID_PARAMETER);
    }
    KnowledgeSource knowledgeSource = store.get(id);
    if(knowledgeSource==null){
        throw new BusinessException(ErrorCode.KNOWLEDGE_SOURCE_NOT_FOUND);
    }
    return knowledgeSource;
 }

 public static User createUser(Map<String, User> users, User user) {
    if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
        throw new BusinessException(ErrorCode.INVALID_PARAMETER);
    }
    if (users.containsKey(user.getUsername())) {
        throw new BusinessException(ErrorCode.USERNAME_EXISTS);
    }
    users.put(user.getUsername(), user);
    return user;
 }
 public static Result<KnowledgeSource> findKnowledgeSourceResult(Map<String,KnowledgeSource>store,String id){
    try {
        return Result.ok(findKnowledgeSource(store, id));//返回成功的结果，包含了KnowledgeSource对象
    } catch (BusinessException exception) {
        return Result.fail(exception.getErrorCode());//返回失败的结果，包含了错误码和错误信息
    }
 }

 public static Result<Ticket> getTicket(Map<String, Ticket> store, String id) {
    try {
        return Result.ok(findTicket(store, id));//返回成功的结果，包含了Ticket对象
    } catch (BusinessException exception) {
        return Result.fail(exception.getErrorCode());//返回失败的结果，包含了错误码和错误信息
    }
}

public static Result<User> createUserResult(Map<String, User> users, User user) {
    try {
        return Result.ok(createUser(users, user));
    } catch (BusinessException exception) {
        return Result.fail(exception.getErrorCode());
    }
}



public static void testException(){

    Map<String, Ticket> store = new HashMap<>();
    Ticket ticket1 = new Ticket("1", "a", "d", "OPEN", "P1", "web", "t", "t");
    Ticket ticket2 = new Ticket("2", "b", "d", "OPEN", "P2", "web", "t", "t");
    Ticket ticket3 = new Ticket("3", "c", "d", "CLOSED", "P1", "web", "t", "t");
    store.put("1", ticket1);
    store.put("2", ticket2);
    store.put("3", ticket3);
    Result<Ticket> found = getTicket(store, "1");
    System.out.println(found.isSuccess() + " " + found.getData().getId());
    // true 1
    Result<Ticket> missing = getTicket(store, "999");
    System.out.println(missing.isSuccess() + " " + missing.getErrorCode());
    // false TICKET_NOT_FOUND
    Result<Ticket> blank = getTicket(store, " ");
System.out.println(blank.isSuccess() + " " + blank.getErrorCode());
// false INVALID_PARAMETER
Map<String, User> users = new HashMap<>();
Result<User> created = createUserResult(users, new User("1", "alice", "a@example.com"));
System.out.println(created.isSuccess() + " " + created.getData().getUsername());
// true alice
Result<User> duplicate = createUserResult(users, new User("2", "alice", "b@example.com"));
System.out.println(duplicate.isSuccess() + " " + duplicate.getErrorCode());
// false USERNAME_EXISTS
Map<String,KnowledgeSource>knowledgeSourceStore=new HashMap<>();
KnowledgeSource knowledgeSource1=new KnowledgeSource("1", KnowledgeSource.SourceType.OFFICIAL_DOC, "product1", "title1", "url1", "license1");
KnowledgeSource knowledgeSource2=new KnowledgeSource("2", KnowledgeSource.SourceType.OFFICIAL_DOC, "product2", "title2", "url2", "license2");
knowledgeSourceStore.put("1", knowledgeSource1);
knowledgeSourceStore.put("2", knowledgeSource2);
Result<KnowledgeSource>foundKnowledgeSource=findKnowledgeSourceResult(knowledgeSourceStore, "1");
System.out.println(foundKnowledgeSource.isSuccess() + " " + foundKnowledgeSource.getData().getId());
// true 1
Result<KnowledgeSource>missingKnowledgeSource=findKnowledgeSourceResult(knowledgeSourceStore, "999");
System.out.println(missingKnowledgeSource.isSuccess() + " " + missingKnowledgeSource.getErrorCode());
// false KNOWLEDGE_SOURCE_NOT_FOUND
Result<KnowledgeSource>blankKnowledgeSource=findKnowledgeSourceResult(knowledgeSourceStore, " ");
System.out.println(blankKnowledgeSource.isSuccess() + " " + blankKnowledgeSource.getErrorCode());
// false INVALID_PARAMETER



}   

    public static void main(String[] args) {
      //  testLinkedListAndArrayListRandomAccessTime();
      //  testEqualsAndHashcode();
        testException();
    }
}
