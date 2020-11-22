package chapter04.FaultTolerance.BubbleFailures;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Worker extends AbstractBehavior<Protocol.Command> {

    public static Behavior<Protocol.Command> create() {
        return Behaviors.setup(Worker::new);
    }

    private Worker(ActorContext<Protocol.Command> context) {
        super(context);

        context.getLog().info("Worker starting up");
    }

    @Override
    public Receive<Protocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Protocol.Fail.class, this::onFail)
                .onMessage(Protocol.Hello.class, this::onHello)
                .build();
    }

    private Behavior<Protocol.Command> onFail(Protocol.Fail message) {
        throw new RuntimeException(message.text);
    }

    private Behavior<Protocol.Command> onHello(Protocol.Hello message) {
        message.replyTo.tell(message.text);
        return this;
    }
}

