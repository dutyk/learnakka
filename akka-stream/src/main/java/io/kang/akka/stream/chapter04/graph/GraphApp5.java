package io.kang.akka.stream.chapter04.graph;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.FanInShape2;
import akka.stream.FlowShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.*;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class GraphApp5 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Source<Integer, NotUsed> ints = Source.fromIterator(() -> new Ints());

        final Flow<Integer, Pair<Integer, String>, NotUsed> pairs =
                Flow.fromGraph(
                        GraphDSL.create(
                                b -> {
                                    final UniformFanOutShape<Integer, Integer> bcast = b.add(Broadcast.create(2));
                                    final FanInShape2<Integer, String, Pair<Integer, String>> zip =
                                            b.add(Zip.create());

                                    b.from(bcast).toInlet(zip.in0());
                                    b.from(bcast)
                                            .via(b.add(Flow.of(Integer.class).map(i -> i.toString())))
                                            .toInlet(zip.in1());

                                    return FlowShape.of(bcast.in(), zip.out());
                                }));

        final CompletionStage<Pair<Integer, String>> firstPair =
                Source.single(1).via(pairs).runWith(Sink.<Pair<Integer, String>>head(), system);

        firstPair.thenAccept(s -> System.out.println(s));
    }
}
