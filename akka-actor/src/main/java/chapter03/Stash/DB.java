package chapter03.Stash;

import akka.Done;

import java.util.concurrent.CompletionStage;

public interface DB {
    CompletionStage<Done> save(String id, String value);

    CompletionStage<String> load(String id);
}
