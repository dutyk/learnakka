package chapter03.ActorDiscovery;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;

public class PingManager {

    interface Command {}

    enum PingAll implements Command {
        INSTANCE
    }

    private static class ListingResponse implements Command {
        final Receptionist.Listing listing;

        private ListingResponse(Receptionist.Listing listing) {
            this.listing = listing;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> new PingManager(context).behavior());
    }

    private final ActorContext<Command> context;
    private final ActorRef<Receptionist.Listing> listingResponseAdapter;

    private PingManager(ActorContext<Command> context) {
        this.context = context;
        this.listingResponseAdapter =
                context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);

        context.spawnAnonymous(PingService.create());
    }

    private Behavior<Command> behavior() {
        return Behaviors.receive(Command.class)
                .onMessage(PingAll.class, notUsed -> onPingAll())
                .onMessage(ListingResponse.class, response -> onListing(response.listing))
                .build();
    }

    private Behavior<Command> onPingAll() {
        context
                .getSystem()
                .receptionist()
                .tell(Receptionist.find(PingService.pingServiceKey, listingResponseAdapter));
        return Behaviors.same();
    }

    private Behavior<Command> onListing(Receptionist.Listing msg) {
        msg.getServiceInstances(PingService.pingServiceKey)
                .forEach(pingService -> context.spawnAnonymous(Pinger.create(pingService)));
        return Behaviors.same();
    }
}
