package chapter12.test.AsynchronousTesting;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Objects;

public class Echo extends AbstractBehavior<Echo.Ping> {
    public Echo(ActorContext<Ping> context) {
        super(context);
    }

    @Override
    public Receive<Ping> createReceive() {
        return newReceiveBuilder().onMessage(
                Ping.class,
                ping -> {
                    System.out.println("ping");
                    getContext().getLog().info("Received message: {}", ping.message);
                    ping.replyTo.tell(new Pong(ping.message));
                    return Behaviors.same();
                })
                .build();
    }

    public static class Ping {
        public final String message;
        public final ActorRef<Pong> replyTo;

        public Ping(String message, ActorRef<Pong> replyTo) {
            this.message = message;
            this.replyTo = replyTo;
        }
    }
    public static class Pong {
        public final String message;

        public Pong(String message) {
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pong)) return false;
            Pong pong = (Pong) o;
            return message.equals(pong.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }
    }

    public static Behavior<Ping> create() {
        return Behaviors.setup(Echo::new);
    }
}