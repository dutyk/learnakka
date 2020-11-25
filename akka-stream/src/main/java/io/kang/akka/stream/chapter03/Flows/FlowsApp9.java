package io.kang.akka.stream.chapter03.Flows;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.CompletionStrategy;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Optional;

public class FlowsApp9 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Flows");

        Source<String, ActorRef> matValuePoweredSource =
                Source.actorRef(
                        elem -> {
                            // complete stream immediately if we send it Done
                            if (elem == Done.done()) return Optional.of(CompletionStrategy.immediately());
                            else return Optional.empty();
                        },
                        // never fail the stream because of a message
                        elem -> Optional.empty(),
                        100,
                        OverflowStrategy.fail());

        Pair<ActorRef, Source<String, NotUsed>> actorRefSourcePair =
                matValuePoweredSource.preMaterialize(system);

        actorRefSourcePair.first().tell("Hello!", ActorRef.noSender());
        actorRefSourcePair.first().tell(Done.done(), ActorRef.noSender());
        actorRefSourcePair.first().tell("Hello!", ActorRef.noSender());

        // pass source around for materialization
        actorRefSourcePair.second().runWith(Sink.foreach(System.out::println), system);
    }
}
