package io.kang.akka.stream.chapter04.graph;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class GraphApp10 {
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
                // #zipping-live
                RunnableGraph.fromGraph(
                        GraphDSL.create(
                                b -> {
                                    final FanInShape2<Integer, Integer, Integer> zip =
                                            b.add(ZipWith.create((Integer left, Integer right) -> left));
                                    final UniformFanOutShape<Integer, Integer> bcast = b.add(Broadcast.create(2));
                                    final UniformFanInShape<Integer, Integer> concat = b.add(Concat.create());
                                    final FlowShape<Integer, Integer> printer = b.add(printFlow);
                                    final SinkShape<Integer> ignore = b.add(Sink.ignore());

                                    b.from(b.add(source)).toInlet(zip.in0());
                                    b.from(zip.out()).via(printer).viaFanOut(bcast).to(ignore);
                                    b.to(zip.in1()).viaFanIn(concat).from(b.add(Source.single(1)));
                                    b.to(concat).fromFanOut(bcast);
                                    return ClosedShape.getInstance();
                                }));
        // #zipping-live

        cyclicSource.run(system);
    }
}
