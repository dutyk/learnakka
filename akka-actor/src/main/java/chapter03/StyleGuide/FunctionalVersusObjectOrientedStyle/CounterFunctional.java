package chapter03.StyleGuide.FunctionalVersusObjectOrientedStyle;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class CounterFunctional {
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

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> counter(context, 0));
    }

    private static Behavior<Command> counter(final ActorContext<Command> context, final int n) {

        return Behaviors.receive(Command.class)
                .onMessage(Increment.class, notUsed -> onIncrement(context, n))
                .onMessage(GetValue.class, command -> onGetValue(n, command))
                .build();
    }

    private static Behavior<Command> onIncrement(ActorContext<Command> context, int n) {
        int newValue = n + 1;
        context.getLog().debug("Incremented counter to [{}]", newValue);
        return counter(context, newValue);
    }

    private static Behavior<Command> onGetValue(int n, GetValue command) {
        command.replyTo.tell(new Value(n));
        return Behaviors.same();
    }
}
