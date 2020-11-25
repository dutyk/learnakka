package io.kang.akka.stream.chapter03.Flows;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class RunWithMyself extends AbstractActor {

    public static Props props() {
        return Props.create(RunWithMyself.class, RunWithMyself::new);
    }

    Materializer mat = Materializer.createMaterializer(context());

    @Override
    public void preStart() throws Exception {
        Source.repeat("hello")
                .runWith(
                        Sink.onComplete(
                                tryDone -> {
                                    System.out.println("Terminated stream: " + tryDone);
                                }),
                        mat);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        String.class,
                        p -> {
                            // this WILL terminate the above stream as well
                            context().stop(self());
                        })
                .build();
    }
}