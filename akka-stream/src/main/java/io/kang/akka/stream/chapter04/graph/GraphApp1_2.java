package io.kang.akka.stream.chapter04.graph;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ClosedShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.*;

import java.util.concurrent.CompletionStage;

public class GraphApp1_2 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Sink<Integer, CompletionStage<Integer>> topHeadSink = Sink.head();
        final Sink<Integer, CompletionStage<Integer>> bottomHeadSink = Sink.head();
        final Flow<Integer, Integer, NotUsed> sharedDoubler =
                Flow.of(Integer.class).map(elem -> elem * 2);

        final RunnableGraph<Pair<CompletionStage<Integer>, CompletionStage<Integer>>> g =
                RunnableGraph.<Pair<CompletionStage<Integer>, CompletionStage<Integer>>>fromGraph(
                        GraphDSL.create(
                                topHeadSink, // import this sink into the graph
                                bottomHeadSink, // and this as well
                                Keep.both(),
                                (b, top, bottom) -> {
                                    final UniformFanOutShape<Integer, Integer> bcast = b.add(Broadcast.create(2));

                                    b.from(b.add(Source.single(1)))
                                            .viaFanOut(bcast)
                                            .via(b.add(sharedDoubler))
                                            .to(top);
                                    b.from(bcast).via(b.add(sharedDoubler)).to(bottom);
                                    return ClosedShape.getInstance();
                                }));
        // #graph-dsl-reusing-a-flow
        final Pair<CompletionStage<Integer>, CompletionStage<Integer>> pair = g.run(system);

        pair.first().thenAccept(s -> System.out.println(s));
        pair.second().thenAccept(s -> System.out.println(s));

    }
}
