package chapter03.Stash;

import akka.Done;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class DBImpl implements DB {

    public static final ConcurrentHashMap<String, String> DBMap = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<Done> save(String id, String value) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DBMap.put(id, value);
        System.out.println("----save-----");

        return CompletableFuture.completedFuture(Done.done());
    }

    @Override
    public CompletionStage<String> load(String id) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("----load-----");
        return CompletableFuture.completedFuture(DBMap.get(id));
    }
}
