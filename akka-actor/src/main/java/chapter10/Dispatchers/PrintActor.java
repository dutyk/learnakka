package chapter10.Dispatchers;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class PrintActor  extends AbstractBehavior<Integer> {

    public static Behavior<Integer> create() {
        return Behaviors.setup(PrintActor::new);
    }

    private PrintActor(ActorContext<Integer> context) {
        super(context);
    }

    @Override
    public Receive<Integer> createReceive() {
        return newReceiveBuilder()
                .onMessage(
                        Integer.class,
                        i -> {
                            System.out.println("PrintActor: " + i);
                            return Behaviors.same();
                        })
                .build();
    }
}