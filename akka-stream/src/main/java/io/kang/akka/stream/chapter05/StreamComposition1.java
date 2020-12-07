package io.kang.akka.stream.chapter05;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class StreamComposition1 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Graphs");

        final Source<Integer, NotUsed> nestedSource =
                Source.single(0) // An atomic source
                        .map(i -> i + 1) // an atomic processing stage
                        .named("nestedSource"); // wraps up the current Source and gives it a name

        final Flow<Integer, Integer, NotUsed> nestedFlow =
                Flow.of(Integer.class)
                        .filter(i -> i != 0) // an atomic processing stage
                        .map(i -> i - 2) // another atomic processing stage
                        .named("nestedFlow"); // wraps up the Flow, and gives it a name

        final Sink<Integer, NotUsed> nestedSink =
                nestedFlow
                        .to(Sink.fold(0, (acc, i) -> acc + i)) // wire an atomic sink to the nestedFlow
                        .named("nestedSink"); // wrap it up

        // Create a RunnableGraph
        final RunnableGraph<NotUsed> runnableGraph = nestedSource.to(nestedSink);

        runnableGraph.run(system);
    }
}
