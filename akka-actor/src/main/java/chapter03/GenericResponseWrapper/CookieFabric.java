package chapter03.GenericResponseWrapper;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;

public class CookieFabric extends AbstractBehavior<CookieFabric.Command> {

    interface Command {}

    public static class GiveMeCookies implements CookieFabric.Command {
        public final int count;
        public final ActorRef<StatusReply<Cookies>> replyTo;

        public GiveMeCookies(int count, ActorRef<StatusReply<CookieFabric.Cookies>> replyTo) {
            this.count = count;
            this.replyTo = replyTo;
        }
    }

    public static class Cookies {
        public final int count;

        public Cookies(int count) {
            this.count = count;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(CookieFabric::new);
    }

    private CookieFabric(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CookieFabric.GiveMeCookies.class, this::onGiveMeCookies)
                .build();
    }

    private Behavior<CookieFabric.Command> onGiveMeCookies(CookieFabric.GiveMeCookies request) {
        if (request.count >= 5) request.replyTo.tell(StatusReply.error("Too many cookies."));
        else request.replyTo.tell(StatusReply.success(new CookieFabric.Cookies(request.count)));

        return this;
    }
}
