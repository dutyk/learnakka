package chapter03.InteractionPatterns.RequestResponsewithaskbetweentwoactors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.time.Duration;

public class Dave extends AbstractBehavior<Dave.Command> {

    public interface Command {}

    // this is a part of the protocol that is internal to the actor itself
    private static final class AdaptedResponse implements Command {
        public final String message;

        public AdaptedResponse(String message) {
            this.message = message;
        }
    }

    public static Behavior<Command> create(ActorRef<Hal.Command> hal) {
        return Behaviors.setup(context -> new Dave(context, hal));
    }

    private Dave(ActorContext<Command> context, ActorRef<Hal.Command> hal) {
        super(context);

        // asking someone requires a timeout, if the timeout hits without response
        // the ask is failed with a TimeoutException
        final Duration timeout = Duration.ofSeconds(3);

        context.ask(
                Hal.HalResponse.class,
                hal,
                timeout,
                // construct the outgoing message
                (ActorRef<Hal.HalResponse> ref) -> new Hal.OpenThePodBayDoorsPlease(ref),
                // adapt the response (or failure to respond)
                (response, throwable) -> {
                    if (response != null) {
                        return new AdaptedResponse(response.message);
                    } else {
                        return new AdaptedResponse("Request failed");
                    }
                });

        // we can also tie in request context into an interaction, it is safe to look at
        // actor internal state from the transformation function, but remember that it may have
        // changed at the time the response arrives and the transformation is done, best is to
        // use immutable state we have closed over like here.
        final int requestId = 1;
        context.ask(
                Hal.HalResponse.class,
                hal,
                timeout,
                // construct the outgoing message
                (ActorRef<Hal.HalResponse> ref) -> new Hal.OpenThePodBayDoorsPlease(ref),
                // adapt the response (or failure to respond)
                (response, throwable) -> {
                    if (response != null) {
                        return new AdaptedResponse(requestId + ": " + response.message);
                    } else {
                        return new AdaptedResponse(requestId + ": Request failed");
                    }
                });
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                // the adapted message ends up being processed like any other
                // message sent to the actor
                .onMessage(AdaptedResponse.class, this::onAdaptedResponse)
                .build();
    }

    private Behavior<Command> onAdaptedResponse(AdaptedResponse response) {
        getContext().getLog().info("Got response from HAL: {}", response.message);
        return this;
    }
}
