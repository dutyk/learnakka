package chapter03.FaultTolerance;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Child {
    static Behavior<String> child(long size) {
        System.out.println("Child start");
        return Behaviors.receiveMessage(msg -> child(size + msg.length()));
    }
}
