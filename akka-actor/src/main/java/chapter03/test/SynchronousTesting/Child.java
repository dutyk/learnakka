package chapter03.test.SynchronousTesting;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Child {
    public static Behavior<String> create() {
        return Behaviors.receive((context, message) -> Behaviors.same());
    }
}
