package chapter03.InteractionPatterns.SendFutureResultToSelf;

import akka.Done;

import java.util.concurrent.CompletionStage;

public interface CustomerDataAccess {
    CompletionStage<Done> update(Customer customer);
}
