package chapter14.StyleGuide.TooManyParameters;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.TimerScheduler;

import java.time.Duration;

public class Counter {
    public interface Command {}

    public static class IncrementRepeatedly implements Command {
        public final Duration interval;

        public IncrementRepeatedly(Duration interval) {
            this.interval = interval;
        }
    }

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

    public static Behavior<Command> create(String name) {
        return Behaviors.setup(
                context -> Behaviors.withTimers(timers -> counter(name, context, timers, 0)));
    }

    private static Behavior<Command> counter(
            final String name,
            final ActorContext<Command> context,
            final TimerScheduler<Command> timers,
            final int n) {

        return Behaviors.receive(Command.class)
                .onMessage(
                        IncrementRepeatedly.class,
                        command -> onIncrementRepeatedly(name, context, timers, n, command))
                .onMessage(Increment.class, notUsed -> onIncrement(name, context, timers, n))
                .onMessage(GetValue.class, command -> onGetValue(n, command))
                .build();
    }

    private static Behavior<Command> onIncrementRepeatedly(
            String name,
            ActorContext<Command> context,
            TimerScheduler<Command> timers,
            int n,
            IncrementRepeatedly command) {
        context
                .getLog()
                .debug(
                        "[{}] Starting repeated increments with interval [{}], current count is [{}]",
                        name,
                        command.interval,
                        n);
        timers.startTimerWithFixedDelay(Increment.INSTANCE, command.interval);
        return Behaviors.same();
    }

    private static Behavior<Command> onIncrement(
            String name, ActorContext<Command> context, TimerScheduler<Command> timers, int n) {
        int newValue = n + 1;
        context.getLog().debug("[{}] Incremented counter to [{}]", name, newValue);
        return counter(name, context, timers, newValue);
    }

    private static Behavior<Command> onGetValue(int n, GetValue command) {
        command.replyTo.tell(new Value(n));
        return Behaviors.same();
    }
}
