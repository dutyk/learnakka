package io.kang.akka.stream.chapter07.DynamicStreamHandling;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.PartitionHub;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class DynamicStreamHandling09 {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final ActorSystem materializer = ActorSystem.create("DynamicStreamHandling");

        Source<Integer, NotUsed> producer = Source.range(0, 100);

        // ConsumerInfo.queueSize is the approximate number of buffered elements for a consumer.
        // Note that this is a moving target since the elements are consumed concurrently.
        RunnableGraph<Source<Integer, NotUsed>> runnableGraph =
                producer.toMat(
                        PartitionHub.ofStateful(
                                Integer.class,
                                () ->
                                        (info, elem) -> {
                                            final List<Object> ids = info.getConsumerIds();
                                            int minValue = info.queueSize(0);
                                            long fastest = info.consumerIdByIdx(0);
                                            for (int i = 1; i < ids.size(); i++) {
                                                int value = info.queueSize(i);
                                                if (value < minValue) {
                                                    minValue = value;
                                                    fastest = info.consumerIdByIdx(i);
                                                }
                                            }
                                            return fastest;
                                        },
                                2,
                                8),
                        Keep.right());

        Source<Integer, NotUsed> fromProducer = runnableGraph.run(materializer);

        fromProducer.runForeach(msg -> System.out.println("consumer1: " + msg), materializer);
        fromProducer
                .throttle(10, Duration.ofMillis(100))
                .runForeach(msg -> System.out.println("consumer2: " + msg), materializer);
    }
}
