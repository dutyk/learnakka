package io.kang.akka.stream.chapter04.graph;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class GraphApp9 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Source<Integer, NotUsed> source = Source.from(Arrays.asList(1, 2, 3, 4, 5));

        final Flow<Integer, Integer, NotUsed> printFlow =
                Flow.of(Integer.class)
                        .map(
                                s -> {
                                    System.out.println(s);
                                    return s;
                                });

        // This cannot produce any value:
        final RunnableGraph<NotUsed> cyclicSource =
                RunnableGraph.fromGraph(
                        GraphDSL.create(
                                b -> {
                                    final UniformFanInShape<Integer, Integer> merge = b.add(Merge.create(2));
                                    final UniformFanOutShape<Integer, Integer> bcast = b.add(Broadcast.create(2));
                                    final FlowShape<Integer, Integer> droppyFlow =
                                            b.add(Flow.of(Integer.class).buffer(10, OverflowStrategy.dropHead()));
                                    final Outlet<Integer> src = b.add(source).out();
                                    final FlowShape<Integer, Integer> printer = b.add(printFlow);
                                    final SinkShape<Integer> ignore = b.add(Sink.ignore());

                                    b.from(src).viaFanIn(merge).via(printer).viaFanOut(bcast).to(ignore);
                                    b.to(merge).via(droppyFlow).fromFanOut(bcast);
                                    return ClosedShape.getInstance();
                                }));
        cyclicSource.run(system);
    }
}
