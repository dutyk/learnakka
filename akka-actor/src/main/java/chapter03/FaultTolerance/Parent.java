package chapter03.FaultTolerance;

import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import static chapter03.FaultTolerance.Child.child;

public class Parent {
    static Behavior<String> create() {
        return Behaviors.<String>supervise(
                Behaviors.setup(
                        ctx -> {
                            // to avoid resource leak
                            final ActorRef<String> child1 = ctx.spawn(child(0), "child1");
                            final ActorRef<String> child2 = ctx.spawn(child(0), "child2");

                            ctx.getLog().info("parent start");

                            return Behaviors.<String>receiveMessage(
                                    msg -> {
                                        // message handling that might throw an exception
                                        String[] parts = msg.split(" ");
                                        child1.tell(parts[0]);
                                        child2.tell(parts[1]);
                                        return Behaviors.same();
                                    });
                        }))
                .onFailure(SupervisorStrategy.restart().withStopChildren(true));
    }
}
