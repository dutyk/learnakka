package io.kang.akka.stream.chapter04.graph;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.FlowShape;
import akka.stream.javadsl.*;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class GraphApp7 {
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



        final Flow<Integer, Integer, CompletionStage<Integer>> foldingFlow =
                Flow.fromGraph(
                        GraphDSL.create(
                                foldSink,
                                (b, fold) -> {
                                    return FlowShape.of(
                                            fold.in(), b.from(b.materializedValue()).via(b.add(flatten)).out());
                                }));

        CompletionStage<Done> result = in.via(foldingFlow).runWith(Sink.foreach(a -> System.out.println(a)), system);
        result.thenAccept(r -> System.out.println(r));
    }
}
