package io.kang.akka.stream.chapter04.graph;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ClosedShape;
import akka.stream.FanInShape2;
import akka.stream.Graph;
import akka.stream.UniformFanInShape;
import akka.stream.javadsl.*;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class GraphApp3 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Graph<FanInShape2<Integer, Integer, Integer>, NotUsed> zip =
                ZipWith.create((Integer left, Integer right) -> Math.max(left, right));

        final Graph<UniformFanInShape<Integer, Integer>, NotUsed> pickMaxOfThree =
                GraphDSL.create(
                        builder -> {
                            final FanInShape2<Integer, Integer, Integer> zip1 = builder.add(zip);
                            final FanInShape2<Integer, Integer, Integer> zip2 = builder.add(zip);

                            builder.from(zip1.out()).toInlet(zip2.in0());
                            // return the shape, which has three inputs and one output
                            return UniformFanInShape.<Integer, Integer>create(
                                    zip2.out(), Arrays.asList(zip1.in0(), zip1.in1(), zip2.in1()));
                        });

        final Sink<Integer, CompletionStage<Integer>> resultSink = Sink.<Integer>head();

        final RunnableGraph<CompletionStage<Integer>> g =
                RunnableGraph.<CompletionStage<Integer>>fromGraph(
                        GraphDSL.create(
                                resultSink,
                                (builder, sink) -> {
                                    // import the partial graph explicitly
                                    final UniformFanInShape<Integer, Integer> pm = builder.add(pickMaxOfThree);

                                    builder.from(builder.add(Source.single(1))).toInlet(pm.in(0));
                                    builder.from(builder.add(Source.single(2))).toInlet(pm.in(1));
                                    builder.from(builder.add(Source.single(3))).toInlet(pm.in(2));
                                    builder.from(pm.out()).to(sink);
                                    return ClosedShape.getInstance();
                                }));

        final CompletionStage<Integer> max = g.run(system);

        max.thenAccept(m -> System.out.println(m));
    }
}
