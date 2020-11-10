package chapter03.InteractionPatterns.SchedulingMessagesToSelf;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Buncher {

    public interface Command {}

    public static final class Batch {
        private final List<Command> messages;

        public Batch(List<Command> messages) {
            this.messages = Collections.unmodifiableList(messages);
        }

        public List<Command> getMessages() {
            return messages;
        }
    }

    public static final class ExcitingMessage implements Command {
        public final String message;

        public ExcitingMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    private static final Object TIMER_KEY = new Object();

    private enum Timeout implements Command {
        INSTANCE
    }

    public static Behavior<Command> create(ActorRef<Batch> target, Duration after, int maxSize) {
        return Behaviors.withTimers(timers -> new Buncher(timers, target, after, maxSize).idle());
    }

    private final TimerScheduler<Command> timers;
    private final ActorRef<Batch> target;
    private final Duration after;
    private final int maxSize;

    private Buncher(
            TimerScheduler<Command> timers, ActorRef<Batch> target, Duration after, int maxSize) {
        this.timers = timers;
        this.target = target;
        this.after = after;
        this.maxSize = maxSize;
    }

    private Behavior<Command> idle() {
        return Behaviors.receive(Command.class)
                .onMessage(Command.class, this::onIdleCommand)
                .build();
    }

    private Behavior<Command> onIdleCommand(Command message) {
        timers.startSingleTimer(TIMER_KEY, Timeout.INSTANCE, after);
        return Behaviors.setup(context -> new Active(context, message));
    }

    private class Active extends AbstractBehavior<Command> {

        private final List<Command> buffer = new ArrayList<>();

        Active(ActorContext<Command> context, Command firstCommand) {
            super(context);
            buffer.add(firstCommand);
        }

        @Override
        public Receive<Command> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Timeout.class, message -> onTimeout())
                    .onMessage(Command.class, this::onCommand)
                    .build();
        }

        private Behavior<Command> onTimeout() {
            target.tell(new Batch(buffer));
            return idle(); // switch to idle
        }

        private Behavior<Command> onCommand(Command message) {
            getContext().getLog().info("{}", message);
            buffer.add(message);
            if (buffer.size() == maxSize) {
                timers.cancel(TIMER_KEY);
                target.tell(new Batch(buffer));
                return idle(); // switch to idle
            } else {
                return this; // stay Active
            }
        }
    }
}
