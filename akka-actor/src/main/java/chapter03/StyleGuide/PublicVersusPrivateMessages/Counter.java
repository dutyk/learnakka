package chapter03.StyleGuide.PublicVersusPrivateMessages;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.time.Duration;

public class Counter extends AbstractBehavior<Counter.Command> {

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

    // Tick is private so can't be sent from the outside
    private enum Tick implements Command {
        INSTANCE
    }

    public static Behavior<Command> create(String name, Duration tickInterval) {
        return Behaviors.setup(
                context ->
                        Behaviors.withTimers(
                                timers -> {
                                    timers.startTimerWithFixedDelay(Tick.INSTANCE, tickInterval);
                                    return new Counter(name, context);
                                }));
    }

    private final String name;
    private int count;

    private Counter(String name, ActorContext<Command> context) {
        super(context);
        this.name = name;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Increment.class, notUsed -> onIncrement())
                .onMessage(Tick.class, notUsed -> onTick())
                .onMessage(GetValue.class, this::onGetValue)
                .build();
    }


    private Behavior<Command> onIncrement() {
        count++;
        getContext().getLog().debug("[{}] Incremented counter to [{}]", name, count);
        return this;
    }

    private Behavior<Command> onTick() {
        count++;
        getContext()
                .getLog()
                .debug("[{}] Incremented counter by background tick to [{}]", name, count);
        return this;
    }

    private Behavior<Command> onGetValue(GetValue command) {
        command.replyTo.tell(new Value(count));
        return this;
    }

}
