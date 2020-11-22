package chapter05.ActorDiscovery;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;

public class Guardian {

    public static Behavior<Void> create() {
        return Behaviors.setup(
                (ActorContext<Receptionist.Listing> context) -> {
                    context
                            .getSystem()
                            .receptionist()
                            .tell(
                                    Receptionist.subscribe(
                                            PingService.pingServiceKey, context.getSelf().narrow()));
                    context.spawnAnonymous(PingService.create());

                    return new Guardian(context).behavior();
                })
                .unsafeCast(); // Void
    }

    private final ActorContext<Receptionist.Listing> context;

    private Guardian(ActorContext<Receptionist.Listing> context) {
        this.context = context;
    }

    private Behavior<Receptionist.Listing> behavior() {
        return Behaviors.receive(Receptionist.Listing.class)
                .onMessage(Receptionist.Listing.class, this::onListing)
                .build();
    }

    private Behavior<Receptionist.Listing> onListing(Receptionist.Listing msg) {
        msg.getServiceInstances(PingService.pingServiceKey)
                .forEach(pingService -> context.spawnAnonymous(Pinger.create(pingService)));
        return Behaviors.same();
    }
}