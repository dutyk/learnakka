package chapter04.FaultTolerance;

import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Counter {
    public interface Command {}

    public static final class Increase implements Command {}

    public static final class Get implements Command {
        public final ActorRef<Got> replyTo;

        public Get(ActorRef<Got> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static final class Got {
        public final int n;

        public Got(int n) {
            this.n = n;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.supervise(Behaviors
                .supervise(counter(1))
                .onFailure(IllegalStateException.class, SupervisorStrategy.restart()))
                .onFailure(IllegalArgumentException.class, SupervisorStrategy.stop());
    }

    private static Behavior<Command> counter(int currentValue) {
        return Behaviors.receive(Command.class)
                .onMessage(Increase.class, o -> onIncrease(currentValue))
                .onMessage(Get.class, command -> onGet(currentValue, command))
                .build();
    }

    private static Behavior<Command> onIncrease(int currentValue) {
        if(currentValue % 2 == 0)
            throw new IllegalStateException();

        return counter(currentValue + 1);
    }

    private static Behavior<Command> onGet(int currentValue, Get command) {
        command.replyTo.tell(new Got(currentValue));
        return Behaviors.same();
    }
}
