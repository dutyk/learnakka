package chapter03.CoordinatedShutdown;

import akka.actor.CoordinatedShutdown;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import static akka.actor.typed.javadsl.AskPattern.ask;
import java.time.Duration;

public class Root extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    ActorRef<MyActor.Messages> myActor = context.spawn(MyActor.create(), "my-actor");
                    ActorSystem<Void> system = context.getSystem();
                    // #coordinated-shutdown-addTask
                    CoordinatedShutdown.get(system).addTask(
                                    CoordinatedShutdown.PhaseBeforeServiceUnbind(),
                                    "someTaskName",
                                    () -> ask(myActor, MyActor.Stop::new, Duration.ofSeconds(1), system.scheduler())
                    );
                    // #coordinated-shutdown-addTask
                    return Behaviors.empty();
                });
    }

    private Root(ActorContext<Void> context) {
        super(context);
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().build();
    }
}
