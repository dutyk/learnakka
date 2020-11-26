package io.kang.akka.stream.chapter04.graph;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ClosedShape;
import akka.stream.Outlet;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class GraphApp1 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Source<Integer, NotUsed> in = Source.from(Arrays.asList(1, 2, 3, 4, 5));
        final Sink<List<String>, CompletionStage<List<String>>> sink = Sink.head();
        final Flow<Integer, Integer, NotUsed> f1 = Flow.of(Integer.class).map(elem -> elem + 10);
        final Flow<Integer, Integer, NotUsed> f2 = Flow.of(Integer.class).map(elem -> elem + 20);
        final Flow<Integer, String, NotUsed> f3 = Flow.of(Integer.class).map(elem -> elem.toString());
        final Flow<Integer, Integer, NotUsed> f4 = Flow.of(Integer.class).map(elem -> elem + 30);

        final RunnableGraph<CompletionStage<List<String>>> result =
                RunnableGraph.fromGraph(
                        GraphDSL // create() function binds sink, out which is sink's out port and builder DSL
                                .create( // we need to reference out's shape in the builder DSL below (in to()
                                        // function)
                                        sink, // previously created sink (Sink)
                                        (builder, out) -> { // variables: builder (GraphDSL.Builder) and out (SinkShape)
                                            final UniformFanOutShape<Integer, Integer> bcast =
                                                    builder.add(Broadcast.create(2));
                                            final UniformFanInShape<Integer, Integer> merge = builder.add(Merge.create(2));

                                            final Outlet<Integer> source = builder.add(in).out();
                                            builder
                                                    .from(source)
                                                    .via(builder.add(f1))
                                                    .viaFanOut(bcast)
                                                    .via(builder.add(f2))
                                                    .viaFanIn(merge)
                                                    .via(builder.add(f3.grouped(1000)))
                                                    .to(out); // to() expects a SinkShape
                                            builder.from(bcast).via(builder.add(f4)).toFanIn(merge);
                                            return ClosedShape.getInstance();
                                        }));
        CompletionStage<List<String>> strs = result.run(system);
        strs.thenAccept(s -> System.out.println(s));
    }
}
