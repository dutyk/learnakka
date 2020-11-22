package chapter14.StyleGuide.TooManyParameters;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.TimerScheduler;

// this is better than previous examples
public class Counter2 {
    // messages omitted for brevity, same messages as above example

    public static Behavior<Counter.Command> create(String name) {
        return Behaviors.setup(
                context ->
                        Behaviors.withTimers(timers -> new Counter2(name, context, timers).counter(0)));
    }

    private final String name;
    private final ActorContext<Counter.Command> context;
    private final TimerScheduler<Counter.Command> timers;

    private Counter2(String name, ActorContext<Counter.Command> context, TimerScheduler<Counter.Command> timers) {
        this.name = name;
        this.context = context;
        this.timers = timers;
    }

    private Behavior<Counter.Command> counter(final int n) {
        return Behaviors.receive(Counter.Command.class)
                .onMessage(Counter.IncrementRepeatedly.class, command -> onIncrementRepeatedly(n, command))
                .onMessage(Counter.Increment.class, notUsed -> onIncrement(n))
                .onMessage(Counter.GetValue.class, command -> onGetValue(n, command))
                .build();
    }

    private Behavior<Counter.Command> onIncrementRepeatedly(int n, Counter.IncrementRepeatedly command) {
        context
                .getLog()
                .debug(
                        "[{}] Starting repeated increments with interval [{}], current count is [{}]",
                        name,
                        command.interval,
                        n);
        timers.startTimerWithFixedDelay(Counter.Increment.INSTANCE, command.interval);
        return Behaviors.same();
    }

    private Behavior<Counter.Command> onIncrement(int n) {
        int newValue = n + 1;
        context.getLog().debug("[{}] Incremented counter to [{}]", name, newValue);
        return counter(newValue);
    }

    private Behavior<Counter.Command> onGetValue(int n, Counter.GetValue command) {
        command.replyTo.tell(new Counter.Value(n));
        return Behaviors.same();
    }
}
