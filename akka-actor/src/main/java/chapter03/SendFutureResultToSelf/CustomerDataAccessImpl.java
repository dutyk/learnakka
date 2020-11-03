package chapter03.SendFutureResultToSelf;

import akka.Done;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CustomerDataAccessImpl implements CustomerDataAccess {
    @Override
    public CompletionStage<Done> update(Customer customer) {
        System.out.println(customer);
        return CompletableFuture.supplyAsync(() -> Done.done());
    }
}
