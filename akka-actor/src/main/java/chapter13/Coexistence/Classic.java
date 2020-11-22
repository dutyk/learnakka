package chapter13.Coexistence;

import akka.actor.AbstractActor;
import akka.actor.typed.javadsl.Adapter;

public class Classic extends AbstractActor {
    public static akka.actor.Props props() {
        return akka.actor.Props.create(Classic.class);
    }

    private final akka.actor.typed.ActorRef<Typed.Command> second =
            Adapter.spawn(getContext(), Typed.behavior(), "second");

    @Override
    public void preStart() {
        Adapter.watch(getContext(), second);
        second.tell(new Typed.Ping(Adapter.toTyped(getSelf())));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        Typed.Pong.class,
                        message -> {
                            Adapter.stop(getContext(), second);
                        })
                .match(
                        akka.actor.Terminated.class,
                        t -> {
                            getContext().stop(getSelf());
                        })
                .build();
    }
}
