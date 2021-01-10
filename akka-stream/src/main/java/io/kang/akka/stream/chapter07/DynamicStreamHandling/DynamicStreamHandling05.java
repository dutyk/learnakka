package io.kang.akka.stream.chapter07.DynamicStreamHandling;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.MergeHub;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class DynamicStreamHandling05 {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final ActorSystem system = ActorSystem.create("DynamicStreamHandling");

        // A simple consumer that will print to the console for now
        Sink<String, CompletionStage<Done>> consumer = Sink.foreach(System.out::println);

        // Attach a MergeHub Source to the consumer. This will materialize to a
        // corresponding Sink.
        RunnableGraph<Sink<String, NotUsed>> runnableGraph = MergeHub.of(String.class, 16).to(consumer);

        // By running/materializing the consumer we get back a Sink, and hence
        // now have access to feed elements into it. This Sink can be materialized
        // any number of times, and every element that enters the Sink will
        // be consumed by our consumer.
        Sink<String, NotUsed> toConsumer = runnableGraph.run(system);

        Source.single("Hello!").runWith(toConsumer, system);
        Source.single("Hub!").runWith(toConsumer, system);
    }
}
