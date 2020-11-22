package chapter03.Coexistence;

import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Adapter;

public class TypedToClassicApp {
    public static void main(String[] args) {
        ActorSystem as = ActorSystem.create();
        ActorRef<Typed1.Command> typed = Adapter.spawn(as, Typed1.create(), "Typed1");
    }
}
