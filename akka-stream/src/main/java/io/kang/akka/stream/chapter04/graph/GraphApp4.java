package io.kang.akka.stream.chapter04.graph;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.FanInShape2;
import akka.stream.SourceShape;
import akka.stream.javadsl.*;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class GraphApp4 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Source<Integer, NotUsed> ints = Source.fromIterator(() -> new Ints());

        final Source<Pair<Object, Object>, NotUsed> pairs =
                Source.fromGraph(
                        GraphDSL.create(
                                builder -> {
                                    final FanInShape2<Object, Object, Pair<Object, Object>> zip =
                                            builder.add(Zip.create());

                                    builder.from(builder.add(ints.filter(i -> i % 2 == 0))).toInlet(zip.in0());
                                    builder.from(builder.add(ints.filter(i -> i % 2 == 1))).toInlet(zip.in1());

                                    return SourceShape.of(zip.out());
                                }));

        final CompletionStage<Pair<Object, Object>> firstPair =
                pairs.runWith(Sink.head(), system);

        firstPair.thenAccept(s -> System.out.println(s));
    }
}
