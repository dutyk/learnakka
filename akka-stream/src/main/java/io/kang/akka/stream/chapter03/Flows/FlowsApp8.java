package io.kang.akka.stream.chapter03.Flows;

import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.Pair;
import akka.stream.FanInShape2;
import akka.stream.FlowShape;
import akka.stream.javadsl.*;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class FlowsApp8 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Flows");

        Duration oneSecond = Duration.ofSeconds(1);
        Flow<Integer, Integer, Cancellable> throttler =
                Flow.fromGraph(
                        GraphDSL.create(
                                Source.tick(oneSecond, oneSecond, ""),
                                (b, tickSource) -> {
                                    FanInShape2<String, Integer, Integer> zip = b.add(ZipWith.create(Keep.right()));
                                    b.from(tickSource).toInlet(zip.in0());
                                    return FlowShape.of(zip.in1(), zip.out());
                                }));

        // An empty source that can be shut down explicitly from the outside
        Source<Integer, CompletableFuture<Optional<Integer>>> source = Source.<Integer>maybe();

        // A flow that internally throttles elements to 1/second, and returns a Cancellable
        // which can be used to shut down the stream
        Flow<Integer, Integer, Cancellable> flow = throttler;

        // A sink that returns the first element of a stream in the returned Future
        Sink<Integer, CompletionStage<Integer>> sink = Sink.head();

        // By default, the materialized value of the leftmost stage is preserved
        RunnableGraph<CompletableFuture<Optional<Integer>>> r1 = source.via(flow).to(sink);

        // Simple selection of materialized values by using Keep.right
        RunnableGraph<Cancellable> r2 = source.viaMat(flow, Keep.right()).to(sink);
        RunnableGraph<CompletionStage<Integer>> r3 = source.via(flow).toMat(sink, Keep.right());

        // Using runWith will always give the materialized values of the stages added
        // by runWith() itself
        CompletionStage<Integer> r4 = source.via(flow).runWith(sink, system);
        CompletableFuture<Optional<Integer>> r5 = flow.to(sink).runWith(source, system);
        Pair<CompletableFuture<Optional<Integer>>, CompletionStage<Integer>> r6 =
                flow.runWith(source, sink, system);

        // Using more complex combinations
        RunnableGraph<Pair<CompletableFuture<Optional<Integer>>, Cancellable>> r7 =
                source.viaMat(flow, Keep.both()).to(sink);

        RunnableGraph<Pair<CompletableFuture<Optional<Integer>>, CompletionStage<Integer>>> r8 =
                source.via(flow).toMat(sink, Keep.both());

        RunnableGraph<
                Pair<Pair<CompletableFuture<Optional<Integer>>, Cancellable>, CompletionStage<Integer>>>
                r9 = source.viaMat(flow, Keep.both()).toMat(sink, Keep.both());

        RunnableGraph<Pair<Cancellable, CompletionStage<Integer>>> r10 =
                source.viaMat(flow, Keep.right()).toMat(sink, Keep.both());

        // It is also possible to map over the materialized values. In r9 we had a
        // doubly nested pair, but we want to flatten it out

        RunnableGraph<Cancellable> r11 =
                r9.mapMaterializedValue(
                        (nestedTuple) -> {
                            CompletableFuture<Optional<Integer>> p = nestedTuple.first().first();
                            Cancellable c = nestedTuple.first().second();
                            CompletionStage<Integer> f = nestedTuple.second();

                            // Picking the Cancellable, but we could  also construct a domain class here
                            return c;
                        });
    }
}
