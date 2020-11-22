package chapter03.StyleGuide.TooManyParameters;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.TimerScheduler;
import chapter03.StyleGuide.TooManyParameters.Counter.*;

public class Counter1 {
    // messages omitted for brevity, same messages as above example

    private static class Setup {
        final String name;
        final ActorContext<Command> context;
        final TimerScheduler<Command> timers;

        private Setup(String name, ActorContext<Command> context, TimerScheduler<Command> timers) {
            this.name = name;
            this.context = context;
            this.timers = timers;
        }
    }

    public static Behavior<Command> create(String name) {
        return Behaviors.setup(
                context ->
                        Behaviors.withTimers(timers -> counter(new Setup(name, context, timers), 0)));
    }

    private static Behavior<Command> counter(final Setup setup, final int n) {

        return Behaviors.receive(Command.class)
                .onMessage(
                        IncrementRepeatedly.class, command -> onIncrementRepeatedly(setup, n, command))
                .onMessage(Increment.class, notUsed -> onIncrement(setup, n))
                .onMessage(GetValue.class, command -> onGetValue(n, command))
                .build();
    }

    private static Behavior<Command> onIncrementRepeatedly(
            Setup setup, int n, IncrementRepeatedly command) {
        setup
                .context
                .getLog()
                .debug(
                        "[{}] Starting repeated increments with interval [{}], current count is [{}]",
                        setup.name,
                        command.interval,
                        n);
        setup.timers.startTimerWithFixedDelay(Increment.INSTANCE, command.interval);
        return Behaviors.same();
    }

    private static Behavior<Command> onIncrement(Setup setup, int n) {
        int newValue = n + 1;
        setup.context.getLog().debug("[{}] Incremented counter to [{}]", setup.name, newValue);
        return counter(setup, newValue);
    }

    private static Behavior<Command> onGetValue(int n, GetValue command) {
        command.replyTo.tell(new Value(n));
        return Behaviors.same();
    }
}
