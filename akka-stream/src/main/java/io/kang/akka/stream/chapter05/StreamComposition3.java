package io.kang.akka.stream.chapter05;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.util.Arrays;

public class StreamComposition3 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Source<Integer, NotUsed> source = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));

        RunnableGraph<NotUsed> graph =  RunnableGraph.fromGraph(
                GraphDSL.create(
                        builder -> {
                            final SourceShape<Integer> A = builder.add(source);
                            final UniformFanOutShape<Integer, Integer> B = builder.add(Broadcast.create(2));
                            final UniformFanInShape<Integer, Integer> C = builder.add(Merge.create(2));
                            final FlowShape<Integer, Integer> D =
                                    builder.add(Flow.of(Integer.class).map(i -> i + 1));
                            final UniformFanOutShape<Integer, Integer> E = builder.add(Balance.create(2));
                            final UniformFanInShape<Integer, Integer> F = builder.add(Merge.create(2));
                            final SinkShape<Integer> G = builder.add(Sink.foreach(System.out::println));

                            builder.from(F.out()).toInlet(C.in(0));
                            builder.from(A).toInlet(B.in());
                            builder.from(B.out(0)).toInlet(C.in(1));
                            builder.from(C.out()).toInlet(F.in(0));
                            builder.from(B.out(1)).via(D).toInlet(E.in());
                            builder.from(E.out(0)).toInlet(F.in(1));
                            builder.from(E.out(1)).to(G);
                            return ClosedShape.getInstance();
                        }));
        // #complex-graph-alt

        graph.run(system);
    }
}
