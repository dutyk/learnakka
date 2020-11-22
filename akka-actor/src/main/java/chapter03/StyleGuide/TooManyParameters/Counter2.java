package chapter03.StyleGuide.TooManyParameters;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.TimerScheduler;
import chapter03.StyleGuide.TooManyParameters.Counter.*;

// this is better than previous examples
public class Counter2 {
    // messages omitted for brevity, same messages as above example

    public static Behavior<Command> create(String name) {
        return Behaviors.setup(
                context ->
                        Behaviors.withTimers(timers -> new Counter2(name, context, timers).counter(0)));
    }

    private final String name;
    private final ActorContext<Command> context;
    private final TimerScheduler<Command> timers;

    private Counter2(String name, ActorContext<Command> context, TimerScheduler<Command> timers) {
        this.name = name;
        this.context = context;
        this.timers = timers;
    }

    private Behavior<Command> counter(final int n) {
        return Behaviors.receive(Command.class)
                .onMessage(IncrementRepeatedly.class, command -> onIncrementRepeatedly(n, command))
                .onMessage(Increment.class, notUsed -> onIncrement(n))
                .onMessage(GetValue.class, command -> onGetValue(n, command))
                .build();
    }

    private Behavior<Command> onIncrementRepeatedly(int n, IncrementRepeatedly command) {
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

    private Behavior<Command> onIncrement(int n) {
        int newValue = n + 1;
        context.getLog().debug("[{}] Incremented counter to [{}]", name, newValue);
        return counter(newValue);
    }

    private Behavior<Command> onGetValue(int n, GetValue command) {
        command.replyTo.tell(new Value(n));
        return Behaviors.same();
    }
}
