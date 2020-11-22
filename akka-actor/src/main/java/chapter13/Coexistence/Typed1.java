package chapter13.Coexistence;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Typed1 extends AbstractBehavior<Typed1.Command> {

    public static class Ping {
        public final akka.actor.typed.ActorRef<Pong> replyTo;

        public Ping(ActorRef<Pong> replyTo) {
            this.replyTo = replyTo;
        }
    }

    interface Command {}

    public enum Pong implements Command {
        INSTANCE
    }

    private final akka.actor.ActorRef second;

    private Typed1(ActorContext<Command> context, akka.actor.ActorRef second) {
        super(context);
        this.second = second;
    }

    public static Behavior<Command> create() {
        return akka.actor.typed.javadsl.Behaviors.setup(
                context -> {
                    akka.actor.ActorRef second = Adapter.actorOf(context, TypedToClassic.props(), "second");

                    Adapter.watch(context, second);

                    second.tell(
                            new Typed1.Ping(context.getSelf().narrow()), Adapter.toClassic(context.getSelf()));

                    return new Typed1(context, second);
                });
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Typed1.Pong.class, message -> onPong())
                .onSignal(akka.actor.typed.Terminated.class, sig -> Behaviors.stopped())
                .build();
    }

    private Behavior<Command> onPong() {
        Adapter.stop(getContext(), second);
        return this;
    }
}
