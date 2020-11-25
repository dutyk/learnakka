package io.kang.akka.stream.chapter03.Flows;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class FlowsApp1 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Flows");

        final Source<Integer, NotUsed> source;
        source = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        // note that the Future is scala.concurrent.Future
        final Sink<Integer, CompletionStage<Integer>> sink =
                Sink.<Integer, Integer>fold(0, (aggr, next) -> aggr + next);


        // connect the Source to the Sink, obtaining a RunnableFlow
        final RunnableGraph<CompletionStage<Integer>> runnable = source.toMat(sink, Keep.right());


        // materialize the flow
        final CompletionStage<Integer> sum = runnable.run(system);

        sum.thenAccept(result -> System.out.println(result));
    }
}
