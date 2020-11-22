package chapter06.Routers;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.GroupRouter;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

public class GroupRouting {
    public static Behavior<Void> showGroupRouting() {
        // #group
        ServiceKey<Worker.Command> serviceKey = ServiceKey.create(Worker.Command.class, "log-worker");

        // #group
        return Behaviors.setup(
                context -> {
                    // #group
                    // this would likely happen elsewhere - if we create it locally we
                    // can just as well use a pool
                    ActorRef<Worker.Command> worker = context.spawn(Worker.create(), "worker");
                    context.getSystem().receptionist().tell(Receptionist.register(serviceKey, worker));

                    GroupRouter<Worker.Command> group = Routers.group(serviceKey);
                    ActorRef<Worker.Command> router = context.spawn(group, "worker-group");

                    // the group router will stash messages until it sees the first listing of registered
                    // services from the receptionist, so it is safe to send messages right away
                    for (int i = 0; i < 10; i++) {
                        router.tell(new Worker.DoLog("msg " + i));
                    }
                    // #group

                    return Behaviors.empty();
                });
    }

}
