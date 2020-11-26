package io.kang.akka.stream.chapter04.graph;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class GraphApp2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create("Graphs");

        // create the source
        final Source<String, NotUsed> in = Source.from(Arrays.asList("ax", "bx", "cx"));
        // generate the sinks from code
        List<String> prefixes = Arrays.asList("a", "b", "c");
        final List<Sink<String, CompletionStage<String>>> list = new ArrayList<>();
        for (String prefix : prefixes) {
            final Sink<String, CompletionStage<String>> sink =
                    Flow.of(String.class)
                            .filter(str -> str.startsWith(prefix))
                            .toMat(Sink.head(), Keep.right());
            list.add(sink);
        }

        final RunnableGraph<List<CompletionStage<String>>> g =
                RunnableGraph.fromGraph(
                        GraphDSL.create(
                                list,
                                (GraphDSL.Builder<List<CompletionStage<String>>> builder,
                                 List<SinkShape<String>> outs) -> {
                                    final UniformFanOutShape<String, String> bcast =
                                            builder.add(Broadcast.create(outs.size()));

                                    final Outlet<String> source = builder.add(in).out();
                                    builder.from(source).viaFanOut(bcast);

                                    for (SinkShape<String> sink : outs) {
                                        builder.from(bcast).to(sink);
                                    }

                                    return ClosedShape.getInstance();
                                }));
        List<CompletionStage<String>> result = g.run(system);
        CompletionStage<List<String>> strs = CompletableFuture.completedFuture(new ArrayList<>());

        for (CompletionStage<String> stringCompletionStage : result) {
            strs = strs.thenCombine(stringCompletionStage, (s1, s2) -> {
                s1.add(s2);
                return s1;
            });
        }

        System.out.println(strs.toCompletableFuture().get());
    }
}
