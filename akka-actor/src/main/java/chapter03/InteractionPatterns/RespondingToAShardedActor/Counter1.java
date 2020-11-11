package chapter03.InteractionPatterns.RespondingToAShardedActor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Counter1 extends AbstractBehavior<Counter1.Command> {

    public interface Command {}

    public enum Increment implements Command {
        INSTANCE
    }

    public static class GetValue implements Command {
        private final ActorRef<Integer> replyTo;

        public GetValue(ActorRef<Integer> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static Behavior<Command> create(String entityId) {
        return Behaviors.setup(context -> new Counter1(context, entityId));
    }

    private final String entityId;
    private int value = 0;

    private Counter1(ActorContext<Command> context, String entityId) {
        super(context);
        this.entityId = entityId;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Increment.class, msg -> onIncrement())
                .onMessage(GetValue.class, this::onGetValue)
                .build();
    }

    private Behavior<Command> onIncrement() {
        value++;
        System.out.println(value);
        return this;
    }

    private Behavior<Command> onGetValue(GetValue msg) {
        msg.replyTo.tell(value);
        return this;
    }
}
