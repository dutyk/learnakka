package chapter14.StyleGuide.TooManyParameters;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.TimerScheduler;

public class Counter1 {
    // messages omitted for brevity, same messages as above example

    private static class Setup {
        final String name;
        final ActorContext<Counter.Command> context;
        final TimerScheduler<Counter.Command> timers;

        private Setup(String name, ActorContext<Counter.Command> context, TimerScheduler<Counter.Command> timers) {
            this.name = name;
            this.context = context;
            this.timers = timers;
        }
    }

    public static Behavior<Counter.Command> create(String name) {
        return Behaviors.setup(
                context ->
                        Behaviors.withTimers(timers -> counter(new Setup(name, context, timers), 0)));
    }

    private static Behavior<Counter.Command> counter(final Setup setup, final int n) {

        return Behaviors.receive(Counter.Command.class)
                .onMessage(
                        Counter.IncrementRepeatedly.class, command -> onIncrementRepeatedly(setup, n, command))
                .onMessage(Counter.Increment.class, notUsed -> onIncrement(setup, n))
                .onMessage(Counter.GetValue.class, command -> onGetValue(n, command))
                .build();
    }

    private static Behavior<Counter.Command> onIncrementRepeatedly(
            Setup setup, int n, Counter.IncrementRepeatedly command) {
        setup
                .context
                .getLog()
                .debug(
                        "[{}] Starting repeated increments with interval [{}], current count is [{}]",
                        setup.name,
                        command.interval,
                        n);
        setup.timers.startTimerWithFixedDelay(Counter.Increment.INSTANCE, command.interval);
        return Behaviors.same();
    }

    private static Behavior<Counter.Command> onIncrement(Setup setup, int n) {
        int newValue = n + 1;
        setup.context.getLog().debug("[{}] Incremented counter to [{}]", setup.name, newValue);
        return counter(setup, newValue);
    }

    private static Behavior<Counter.Command> onGetValue(int n, Counter.GetValue command) {
        command.replyTo.tell(new Counter.Value(n));
        return Behaviors.same();
    }
}
