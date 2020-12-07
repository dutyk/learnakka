package io.kang.akka.stream.chapter05;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;
import java.util.Arrays;

public class StreamComposition4 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Source<Integer, NotUsed> source = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));

        final Graph<FlowShape<Integer, Integer>, NotUsed> partial =
                GraphDSL.create(
                        builder -> {
                            final UniformFanOutShape<Integer, Integer> B = builder.add(Broadcast.create(2));
                            final UniformFanInShape<Integer, Integer> C = builder.add(Merge.create(2));
                            final UniformFanOutShape<Integer, Integer> E = builder.add(Balance.create(2));
                            final UniformFanInShape<Integer, Integer> F = builder.add(Merge.create(2));

                            builder.from(F.out()).toInlet(C.in(0));
                            builder.from(B).viaFanIn(C).toFanIn(F);
                            builder
                                    .from(B)
                                    .via(builder.add(Flow.of(Integer.class).map(i -> i + 1)))
                                    .viaFanOut(E)
                                    .toFanIn(F);

                            return new FlowShape<Integer, Integer>(B.in(), E.out(1));
                        });
        // #complex-graph-alt
        source.via(partial).to(Sink.foreach(System.out::println)).run(system);
    }
}
