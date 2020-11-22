package chapter14.StyleGuide.PublicVersusPrivateMessages;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;

public class Counter1 extends AbstractBehavior<Counter1.Message> {

    // The type of all public and private messages the Counter actor handles
    public interface Message {}

    /** Counter's public message protocol type. */
    public interface Command extends Message {}

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

    // The type of the Counter actor's internal messages.
    interface PrivateCommand extends Message {}

    // Tick is a private command so can't be sent to an ActorRef<Command>
    enum Tick implements PrivateCommand {
        INSTANCE
    }

    public static Behavior<Command> create(String name, Duration tickInterval) {
        return Behaviors.setup(
                (ActorContext<Message> context) ->
                        Behaviors.withTimers(
                                timers -> {
                                    timers.startTimerWithFixedDelay(Tick.INSTANCE, tickInterval);
                                    return new Counter1(name, context);
                                }))
                .narrow(); // note narrow here
    }

    private final String name;
    private int count;

    private Counter1(String name, ActorContext<Message> context) {
        super(context);
        this.name = name;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Increment.class, notUsed -> onIncrement())
                .onMessage(Tick.class, notUsed -> onTick())
                .onMessage(GetValue.class, this::onGetValue)
                .build();
    }

    private Behavior<Message> onIncrement() {
        count++;
        getContext().getLog().debug("[{}] Incremented counter to [{}]", name, count);
        return this;
    }

    private Behavior<Message> onTick() {
        count++;
        getContext()
                .getLog()
                .debug("[{}] Incremented counter by background tick to [{}]", name, count);
        return this;
    }

    private Behavior<Message> onGetValue(GetValue command) {
        command.replyTo.tell(new Value(count));
        return this;
    }
}
