package chapter14.StyleGuide.WhereToDefineMessages;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class Counter extends AbstractBehavior<Counter.Command> {

    public Counter(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return null;
    }

    public interface Command {}

    public enum Increment implements Command {
        INSTANCE
    }

    public static class GetValue implements Command {
        public final ActorRef<Value> replyTo;

        public GetValue(ActorRef<Value> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class Value {
        public final int value;

        public Value(int value) {
            this.value = value;
        }
    }
}
