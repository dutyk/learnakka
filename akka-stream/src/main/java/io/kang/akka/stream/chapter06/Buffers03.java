package io.kang.akka.stream.chapter06;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.stream.Attributes;
import akka.stream.ClosedShape;
import akka.stream.FanInShape2;
import akka.stream.javadsl.*;

import java.time.Duration;

public class Buffers03 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Buffers");

        final Duration oneSecond = Duration.ofSeconds(1);
        final Source<String, Cancellable> msgSource = Source.tick(oneSecond, oneSecond, "message!");
        final Source<String, Cancellable> tickSource =
                Source.tick(oneSecond.multipliedBy(3), oneSecond.multipliedBy(3), "tick");
        final Flow<String, Integer, NotUsed> conflate =
                Flow.of(String.class).conflateWithSeed(first -> 1, (count, elem) -> count + 1);

        RunnableGraph.fromGraph(
                GraphDSL.create(
                        b -> {
                            // this is the asynchronous stage in this graph
                            final FanInShape2<String, Integer, Integer> zipper =
                                    b.add(ZipWith.create((String tick, Integer count) -> count).async().withAttributes(Attributes.inputBuffer(1,1)));
                            b.from(b.add(msgSource)).via(b.add(conflate)).toInlet(zipper.in1());
                            b.from(b.add(tickSource)).toInlet(zipper.in0());
                            b.from(zipper.out()).to(b.add(Sink.foreach(elem -> System.out.println(elem))));
                            return ClosedShape.getInstance();
                        }))
                .run(system);
    }
}
