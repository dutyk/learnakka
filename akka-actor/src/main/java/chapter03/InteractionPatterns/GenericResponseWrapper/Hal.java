package chapter03.InteractionPatterns.GenericResponseWrapper;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;

public class Hal extends AbstractBehavior<Hal.Command> {

    public static Behavior<Command> create() {
        return Behaviors.setup(Hal::new);
    }

    private Hal(ActorContext<Command> context) {
        super(context);
    }

    public interface Command {
    }

    public static final class OpenThePodBayDoorsPlease implements Hal.Command {
        public final ActorRef<StatusReply<String>> respondTo;

        public OpenThePodBayDoorsPlease(ActorRef<StatusReply<String>> respondTo) {
            this.respondTo = respondTo;
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Hal.OpenThePodBayDoorsPlease.class, this::onOpenThePodBayDoorsPlease)
                .build();
    }

    private Behavior<Hal.Command> onOpenThePodBayDoorsPlease(
            Hal.OpenThePodBayDoorsPlease message) {
        message.respondTo.tell(StatusReply.error("I'm sorry, Dave. I'm afraid I can't do that."));
        return this;
    }
}