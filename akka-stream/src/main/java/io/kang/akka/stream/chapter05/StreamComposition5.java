package io.kang.akka.stream.chapter05;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;
import java.util.Arrays;

public class StreamComposition5 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Graphs");

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
        // #partial-flow-dsl
        // Convert the partial graph of FlowShape to a Flow to get
        // access to the fluid DSL (for example to be able to call .filter())
        final Flow<Integer, Integer, NotUsed> flow = Flow.fromGraph(partial);

        // Simple way to create a graph backed Source
        final Source<Integer, NotUsed> source =
                Source.fromGraph(
                        GraphDSL.create(
                                builder -> {
                                    final UniformFanInShape<Integer, Integer> merge = builder.add(Merge.create(2));
                                    builder.from(builder.add(Source.single(0))).toFanIn(merge);
                                    builder.from(builder.add(Source.from(Arrays.asList(2, 3, 4)))).toFanIn(merge);
                                    // Exposing exactly one output port
                                    return new SourceShape<Integer>(merge.out());
                                }));

        // Building a Sink with a nested Flow, using the fluid DSL
        final Sink<Integer, NotUsed> sink =
                Flow.of(Integer.class).map(i -> i * 2).named("nestedFlow").to(Sink.foreach(System.out::println));

        // Putting all together
        final RunnableGraph<NotUsed> closed = source.via(flow.filter(i -> i > 1)).to(sink);
        // #partial-flow-dsl
        closed.run(system);
    }
}
