package chapter03.Coexistence;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Typed {
    interface Command {}

    public static class Ping implements Command {
        public final akka.actor.typed.ActorRef<Pong> replyTo;

        public Ping(ActorRef<Pong> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class Pong {}

    public static Behavior<Command> behavior() {
        return Behaviors.receive(Command.class)
                .onMessage(
                        Ping.class,
                        message -> {
                            System.out.println(message);
                            message.replyTo.tell(new Pong());
                            return Behaviors.same();
                        })
                .build();
    }
}
