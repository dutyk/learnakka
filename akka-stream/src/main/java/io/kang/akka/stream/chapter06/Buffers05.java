package io.kang.akka.stream.chapter06;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.tuple.Tuple3;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class Buffers05 {
    static class Job {}

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Buffers");

        final Flow<Double, Tuple3<Double, Double, Integer>, NotUsed> statsFlow =
                Flow.of(Double.class)
                        .conflateWithSeed(
                                elem -> Collections.singletonList(elem),
                                (acc, elem) -> {
                                    return Stream.concat(acc.stream(), Collections.singletonList(elem).stream())
                                            .collect(Collectors.toList());
                                })
                        .map(
                                s -> {
                                    final Double mean = s.stream().mapToDouble(d -> d).sum() / s.size();
                                    final DoubleStream se = s.stream().mapToDouble(x -> Math.pow(x - mean, 2));
                                    final Double stdDev = Math.sqrt(se.sum() / s.size());
                                    return new Tuple3<>(stdDev, mean, s.size());
                                });
        Source.single(2.0).via(statsFlow).runWith(Sink.foreach(System.out::println), system);
    }
}
