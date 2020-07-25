package io.kang.akka.iot.part1.failurehandling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;

public class App {
    public static void main(String[] args) {
        ActorRef<String> supervisingActor =
                ActorSystem.create(SupervisingActor.create(), "supervising-actor");
        supervisingActor.tell("failChild");
    }
}
