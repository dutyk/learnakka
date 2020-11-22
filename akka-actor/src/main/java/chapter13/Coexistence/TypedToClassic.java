package chapter13.Coexistence;

import akka.actor.AbstractActor;

public class TypedToClassic extends AbstractActor {
    public static akka.actor.Props props() {
        return akka.actor.Props.create(TypedToClassic.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Typed1.Ping.class, this::onPing).build();
    }

    private void onPing(Typed1.Ping message) {
        message.replyTo.tell(Typed1.Pong.INSTANCE);
    }
}