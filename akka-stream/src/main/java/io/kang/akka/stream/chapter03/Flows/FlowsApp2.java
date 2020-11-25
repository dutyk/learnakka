package io.kang.akka.stream.chapter03.Flows;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class FlowsApp2 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Flows");

        final Source<Integer, NotUsed> source =
                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        final Sink<Integer, CompletionStage<Integer>> sink =
                Sink.<Integer, Integer>fold(0, (aggr, next) -> aggr + next);


        // materialize the flow, getting the Sinks materialized value
        final CompletionStage<Integer> sum = source.runWith(sink, system);

        sum.thenAccept(result -> System.out.println(result));
    }
}
