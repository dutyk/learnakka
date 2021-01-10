package io.kang.akka.stream.chapter07.DynamicStreamHandling;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.stream.javadsl.BroadcastHub;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class DynamicStreamHandling06 {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final ActorSystem system = ActorSystem.create("DynamicStreamHandling");

        // A simple producer that publishes a new "message" every second
        Source<String, Cancellable> producer =
                Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(1), "New message");


        // Attach a BroadcastHub Sink to the producer. This will materialize to a
        // corresponding Source.
        // (We need to use toMat and Keep.right since by default the materialized
        // value to the left is used)
        RunnableGraph<Source<String, NotUsed>> runnableGraph =
                producer.toMat(BroadcastHub.of(String.class, 256), Keep.right());

        // By running/materializing the producer, we get back a Source, which
        // gives us access to the elements published by the producer.
        Source<String, NotUsed> fromProducer = runnableGraph.run(system);

        // Print out messages from the producer in two independent consumers
        fromProducer.runForeach(msg -> System.out.println("consumer1: " + msg), system);
        fromProducer.runForeach(msg -> System.out.println("consumer2: " + msg), system);
    }
}
