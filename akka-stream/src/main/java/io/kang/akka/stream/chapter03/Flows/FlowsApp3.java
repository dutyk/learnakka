package io.kang.akka.stream.chapter03.Flows;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class FlowsApp3 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Flows");

        final Source<Integer, NotUsed> source =
                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        source.map(x -> 0); // has no effect on source, since it's immutable
        source.runWith(Sink.fold(0, (agg, next) -> agg + next), system); // 55

        // returns new Source<Integer>, with `map()` appended
        final Source<Integer, NotUsed> zeroes = source.map(x -> 0);

        final Sink<Integer, CompletionStage<Integer>> fold =
                Sink.<Integer, Integer>fold(0, (agg, next) -> agg + next);

        final CompletionStage<Integer> result = zeroes.runWith(fold, system); // 0

        result.thenAccept(r -> System.out.println(r));

    }
}
