package io.kang.akka.stream.chapter07.DynamicStreamHandling;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.PartitionHub;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.ToLongBiFunction;

public class DynamicStreamHandling08 {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final ActorSystem materializer = ActorSystem.create("DynamicStreamHandling");

        // A simple producer that publishes a new "message-n" every second
        Source<String, Cancellable> producer =
                Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(1), "message")
                        .zipWith(Source.range(0, 100), (a, b) -> a + "-" + b);

        // Attach a PartitionHub Sink to the producer. This will materialize to a
        // corresponding Source.
        // (We need to use toMat and Keep.right since by default the materialized
        // value to the left is used)
        RunnableGraph<Source<String, NotUsed>> runnableGraph =
                producer.toMat(
                        PartitionHub.ofStateful(String.class, () -> new RoundRobin<String>(), 2, 256),
                        Keep.right());

        // By running/materializing the producer, we get back a Source, which
        // gives us access to the elements published by the producer.
        Source<String, NotUsed> fromProducer = runnableGraph.run(materializer);

        // Print out messages from the producer in two independent consumers
        fromProducer.runForeach(msg -> System.out.println("consumer1: " + msg), materializer);
        fromProducer.runForeach(msg -> System.out.println("consumer2: " + msg), materializer);
    }

    // Using a class since variable must otherwise be final.
    // New instance is created for each materialization of the PartitionHub.
    static class RoundRobin<T> implements ToLongBiFunction<PartitionHub.ConsumerInfo, T> {

        private long i = -1;

        @Override
        public long applyAsLong(PartitionHub.ConsumerInfo info, T elem) {
            i++;
            return info.consumerIdByIdx((int) (i % info.size()));
        }
    }
}
