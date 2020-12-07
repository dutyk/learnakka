package io.kang.akka.stream.chapter05;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.util.Arrays;

public class StreamComposition2 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Source<Integer, NotUsed> source = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));

        RunnableGraph<NotUsed> graph = RunnableGraph.fromGraph(
                GraphDSL.create(
                        builder -> {
                            final Outlet<Integer> A = builder.add(source).out();
                            final UniformFanOutShape<Integer, Integer> B = builder.add(Broadcast.create(2));
                            final UniformFanInShape<Integer, Integer> C = builder.add(Merge.create(2));
                            final FlowShape<Integer, Integer> D =
                                    builder.add(Flow.of(Integer.class).map(i -> i));
                            final UniformFanOutShape<Integer, Integer> E = builder.add(Broadcast.create(2));
                            final UniformFanInShape<Integer, Integer> F = builder.add(Merge.create(2));
                            final Inlet<Integer> G = builder.add(Sink.<Integer>foreach(System.out::println)).in();

                            builder.from(F).toFanIn(C);
                            builder.from(A).viaFanOut(B).viaFanIn(C).toFanIn(F);
                            builder.from(B).via(D).viaFanOut(E).toFanIn(F);
                            builder.from(E).toInlet(G);
                            return ClosedShape.getInstance();
                        }));

        graph.run(system);
    }
}
