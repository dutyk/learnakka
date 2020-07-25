package io.kang.akka.iot.part1.actorlifecycle;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.ActorRef;

public class App {
    public static void main(String[] args) {
        ActorRef<String> testSystem = ActorSystem.create(StartStopActor1.create(), "startStopActor1");
        testSystem.tell("stop");
    }
}
