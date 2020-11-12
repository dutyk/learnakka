package chapter03.FaultTolerance.BubbleFailures;

import akka.actor.typed.DeathPactException;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Boss extends AbstractBehavior<Protocol.Command> {

    public static Behavior<Protocol.Command> create() {
        return Behaviors.supervise(Behaviors.setup(Boss::new))
                .onFailure(DeathPactException.class, SupervisorStrategy.restart());
    }

    private final ActorRef<Protocol.Command> middleManagement;

    private Boss(ActorContext<Protocol.Command> context) {
        super(context);
        context.getLog().info("Boss starting up");
        // default supervision of child, meaning that it will stop on failure
        middleManagement = context.spawn(MiddleManagement.create(), "middle-management");
        context.watch(middleManagement);
    }

    @Override
    public Receive<Protocol.Command> createReceive() {
        // here we don't handle Terminated at all which means that
        // when middle management fails with a DeathPactException
        // this actor will also fail
        return newReceiveBuilder().onMessage(Protocol.Command.class, this::onCommand).build();
    }

    private Behavior<Protocol.Command> onCommand(Protocol.Command message) {
        // just pass messages on to the child
        middleManagement.tell(message);
        return this;
    }
}
