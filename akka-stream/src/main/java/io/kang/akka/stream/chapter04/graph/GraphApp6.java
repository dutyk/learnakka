package io.kang.akka.stream.chapter04.graph;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class GraphApp6 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create("Graphs");

        Source<Integer, NotUsed> source1 = Source.single(1);
        Source<Integer, NotUsed> source2 = Source.single(2);

        final Source<Integer, NotUsed> sources =
                Source.combine(source1, source2, new ArrayList<>(), i -> Merge.<Integer>create(i));

        CompletionStage<Integer> result = sources.runWith(Sink.<Integer, Integer>fold(0, (a, b) -> a + b), system);

        result.thenAccept(s -> System.out.println(s));
    }
}
