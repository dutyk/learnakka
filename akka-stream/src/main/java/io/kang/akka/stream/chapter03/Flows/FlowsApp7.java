package io.kang.akka.stream.chapter03.Flows;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;

public class FlowsApp7 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Flows");

        Source.range(1, 3).map(x -> x + 1).async().map(x -> x * 2).to(Sink.foreach(System.out::println)).run(system);
    }
}
