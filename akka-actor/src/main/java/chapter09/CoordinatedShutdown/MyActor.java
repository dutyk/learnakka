package chapter09.CoordinatedShutdown;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class MyActor extends AbstractBehavior<MyActor.Messages> {
    interface Messages {}

    // ...

    static final class Stop implements Messages {
        final ActorRef<Done> replyTo;

        Stop(ActorRef<Done> replyTo) {
            this.replyTo = replyTo;
        }
    }
    // #coordinated-shutdown-addTask

    public static Behavior<Messages> create() {
        return Behaviors.setup(MyActor::new);
    }

    private MyActor(ActorContext<Messages> context) {
        super(context);
    }

    // #coordinated-shutdown-addTask
    @Override
    public Receive<Messages> createReceive() {
        return newReceiveBuilder().onMessage(Stop.class, this::stop).build();
    }

    private Behavior<Messages> stop(Stop stop) {
        // shut down the actor internal
        // ...
        getContext().getLog().info("stop msg");
        stop.replyTo.tell(Done.done());
        return Behaviors.stopped();
    }
}
