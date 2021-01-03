package io.kang.akka.stream.chapter06;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Buffers06 {
    static class Job {}

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Buffers");

        final Double p = 0.01;
        final Random r = new Random();
        final Flow<Double, Double, NotUsed> sampleFlow =
                Flow.of(Double.class)
                        .conflateWithSeed(
                                elem -> Collections.singletonList(elem),
                                (acc, elem) -> {
                                    if (r.nextDouble() < p) {
                                        return Stream.concat(acc.stream(), Collections.singletonList(elem).stream())
                                                .collect(Collectors.toList());
                                    }
                                    return acc;
                                })
                        .mapConcat(d -> d);

        Source.single(2.0).via(sampleFlow).runWith(Sink.foreach(System.out::println), system);
    }
}
