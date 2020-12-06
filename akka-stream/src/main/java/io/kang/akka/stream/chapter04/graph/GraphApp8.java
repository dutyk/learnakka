package io.kang.akka.stream.chapter04.graph;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.SourceShape;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class GraphApp8 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Source<Integer, NotUsed> in = Source.from(Arrays.asList(1, 2, 3, 4, 5));

        final Sink<Integer, CompletionStage<Integer>> foldSink =
                Sink.<Integer, Integer>fold(
                        0,
                        (a, b) -> {
                            return a + b;
                        });

        final Flow<CompletionStage<Integer>, Integer, NotUsed> flatten =
                Flow.<CompletionStage<Integer>>create().mapAsync(4, x -> x);



        // This cannot produce any value:
        final Source<Integer, CompletionStage<Integer>> cyclicSource =
                Source.fromGraph(
                        GraphDSL.create(
                                foldSink,
                                (b, fold) -> {
                                    // - Fold cannot complete until its upstream mapAsync completes
                                    // - mapAsync cannot complete until the materialized Future produced by
                                    //   fold completes
                                    // As a result this Source will never emit anything, and its materialited
                                    // Future will never complete
                                    b.from(b.materializedValue()).via(b.add(flatten)).to(fold);
                                    return SourceShape.of(b.from(b.materializedValue()).via(b.add(flatten)).out());
                                }));

        CompletionStage<Done> result = cyclicSource.runWith(Sink.foreach(a -> System.out.println(a)), system);
        result.thenAccept(r -> System.out.println(r));

    }
}
