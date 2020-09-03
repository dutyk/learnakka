package chapter03.RequestResponsewithaskbetweentwoactors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Hal extends AbstractBehavior<Hal.Command> {

    public static Behavior<Command> create() {
        return Behaviors.setup(Hal::new);
    }

    private Hal(ActorContext<Command> context) {
        super(context);
    }

    public interface Command {}

    public static final class OpenThePodBayDoorsPlease implements Command {
        public final ActorRef<HalResponse> respondTo;

        public OpenThePodBayDoorsPlease(ActorRef<HalResponse> respondTo) {
            this.respondTo = respondTo;
        }
    }

    public static final class HalResponse {
        public final String message;

        public HalResponse(String message) {
            this.message = message;
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(OpenThePodBayDoorsPlease.class, this::onOpenThePodBayDoorsPlease)
                .build();
    }

    private Behavior<Command> onOpenThePodBayDoorsPlease(OpenThePodBayDoorsPlease message) {
        message.respondTo.tell(new HalResponse("I'm sorry, Dave. I'm afraid I can't do that."));
        return this;
    }
}
