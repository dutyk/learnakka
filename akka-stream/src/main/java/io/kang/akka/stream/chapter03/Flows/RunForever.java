package io.kang.akka.stream.chapter03.Flows;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class RunForever extends AbstractActor {

    private final Materializer materializer;

    public static Props props(Materializer materializer) {
        return Props.create(RunForever.class, () -> new RunForever(materializer));
    }

    public RunForever(Materializer materializer) {
        this.materializer = materializer;
    }

    @Override
    public void preStart() throws Exception {
        Source.repeat("hello")
                .runWith(
                        Sink.onComplete(
                                tryDone -> {
                                    System.out.println("Terminated stream: " + tryDone);
                                }),
                        materializer);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        String.class,
                        p -> {
                            // will NOT terminate the stream (it's bound to the system!)
                            context().stop(self());
                        })
                .build();
    }
}