package io.kang.akka.stream.chapter06;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.Attributes;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class Buffers02 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Buffers");

        final Flow<Integer, Integer, NotUsed> flow1 =
                Flow.of(Integer.class)
                        .map(elem -> elem * 2)
                        .async()
                        .addAttributes(Attributes.inputBuffer(1, 1)); // the buffer size of this map is 1

        final Flow<Integer, Integer, NotUsed> flow2 =
                flow1
                        .via(Flow.of(Integer.class).map(elem -> elem / 2))
                        .async(); // the buffer size of this map is the value from the surrounding graph it is
        // used in
        final RunnableGraph<NotUsed> runnableGraph =
                Source.range(1, 10).via(flow1).to(Sink.foreach(elem -> System.out.println(elem)));

        final RunnableGraph<NotUsed> withOverridenDefaults =
                runnableGraph.withAttributes(Attributes.inputBuffer(64, 64));

        runnableGraph.run(system);
    }
}
