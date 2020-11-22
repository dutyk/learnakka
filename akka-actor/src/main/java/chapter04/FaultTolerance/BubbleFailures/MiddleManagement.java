package chapter04.FaultTolerance.BubbleFailures;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class MiddleManagement extends AbstractBehavior<Protocol.Command> {

    public static Behavior<Protocol.Command> create() {
        return Behaviors.setup(MiddleManagement::new);
    }

    private final ActorRef<Protocol.Command> child;

    private MiddleManagement(ActorContext<Protocol.Command> context) {
        super(context);

        context.getLog().info("Middle management starting up");
        // default supervision of child, meaning that it will stop on failure
        child = context.spawn(Worker.create(), "child");

        // we want to know when the child terminates, but since we do not handle
        // the Terminated signal, we will in turn fail on child termination
        context.watch(child);
    }

    @Override
    public Receive<Protocol.Command> createReceive() {
        // here we don't handle Terminated at all which means that
        // when the child fails or stops gracefully this actor will
        // fail with a DeathPactException
        return newReceiveBuilder().onMessage(Protocol.Command.class, this::onCommand).build();
    }

    private Behavior<Protocol.Command> onCommand(Protocol.Command message) {
        // just pass messages on to the child
        child.tell(message);
        return this;
    }
}
