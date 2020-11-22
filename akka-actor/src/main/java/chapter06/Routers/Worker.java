package chapter06.Routers;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class Worker {
    interface Command {}

    static class DoLog implements Command {
        public final String text;

        public DoLog(String text) {
            this.text = text;
        }
    }

    static final Behavior<Command> create() {
        return Behaviors.setup(
                context -> {
                    context.getLog().info("Starting worker");

                    return Behaviors.receive(Command.class)
                            .onMessage(DoLog.class, doLog -> onDoLog(context, doLog))
                            .build();
                });
    }

    private static Behavior<Command> onDoLog(ActorContext<Command> context, DoLog doLog) {
        context.getLog().info("Got message {}", doLog.text);
        return Behaviors.same();
    }
}
