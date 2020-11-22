package chapter05.ActorDiscovery;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class Pinger {
    private final ActorContext<PingService.Pong> context;
    private final ActorRef<PingService.Ping> pingService;

    private Pinger(ActorContext<PingService.Pong> context, ActorRef<PingService.Ping> pingService) {
        this.context = context;
        this.pingService = pingService;
    }

    public static Behavior<PingService.Pong> create(ActorRef<PingService.Ping> pingService) {
        return Behaviors.setup(
                ctx -> {
                    pingService.tell(new PingService.Ping(ctx.getSelf()));
                    return new Pinger(ctx, pingService).behavior();
                });
    }

    private Behavior<PingService.Pong> behavior() {
        return Behaviors.receive(PingService.Pong.class)
                .onMessage(PingService.Pong.class, this::onPong)
                .build();
    }

    private Behavior<PingService.Pong> onPong(PingService.Pong msg) {
        context.getLog().info("{} was ponged!!", context.getSelf());
        return Behaviors.stopped();
    }
}
