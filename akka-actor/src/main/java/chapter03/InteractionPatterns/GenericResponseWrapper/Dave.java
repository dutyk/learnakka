package chapter03.InteractionPatterns.GenericResponseWrapper;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;

import java.time.Duration;

public class Dave extends AbstractBehavior<Dave.Command> {

    public interface Command {}

    // this is a part of the protocol that is internal to the actor itself
    private static final class AdaptedResponse implements Dave.Command {
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

        context.askWithStatus(
                String.class,
                hal,
                timeout,
                // construct the outgoing message
                (ActorRef<StatusReply<String>> ref) -> new Hal.OpenThePodBayDoorsPlease(ref),
                // adapt the response (or failure to respond)
                (response, throwable) -> {
                    if (response != null) {
                        // a ReponseWithStatus.success(m) is unwrapped and passed as response
                        return new Dave.AdaptedResponse(response);
                    } else {
                        // a ResponseWithStatus.error will end up as a StatusReply.ErrorMessage()
                        // exception here
                        return new Dave.AdaptedResponse("Request failed: " + throwable.getMessage());
                    }
                });
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                // the adapted message ends up being processed like any other
                // message sent to the actor
                .onMessage(Dave.AdaptedResponse.class, this::onAdaptedResponse)
                .build();
    }

    private Behavior<Dave.Command> onAdaptedResponse(Dave.AdaptedResponse response) {
        getContext().getLog().info("Got response from HAL: {}", response.message);
        return this;
    }
}

