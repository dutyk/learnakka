package chapter01.functionstyle;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class Gabbler {
    public static Behavior<ChatRoom.SessionEvent> create() {
        return Behaviors.setup(ctx -> new Gabbler(ctx).behavior());
    }

    private final ActorContext<ChatRoom.SessionEvent> context;

    private Gabbler(ActorContext<ChatRoom.SessionEvent> context) {
        this.context = context;
    }

    private Behavior<ChatRoom.SessionEvent> behavior() {
        return Behaviors.receive(ChatRoom.SessionEvent.class)
                .onMessage(ChatRoom.SessionDenied.class, this::onSessionDenied)
                .onMessage(ChatRoom.SessionGranted.class, this::onSessionGranted)
                .onMessage(ChatRoom.MessagePosted.class, this::onMessagePosted)
                .build();
    }

    private Behavior<ChatRoom.SessionEvent> onSessionDenied(ChatRoom.SessionDenied message) {
        context.getLog().info("cannot start chat room session: {}", message.reason);
        return Behaviors.stopped();
    }

    private Behavior<ChatRoom.SessionEvent> onSessionGranted(ChatRoom.SessionGranted message) {
        message.handle.tell(new ChatRoom.PostMessage("Hello World!"));
        return Behaviors.same();
    }

    private Behavior<ChatRoom.SessionEvent> onMessagePosted(ChatRoom.MessagePosted message) {
        context
                .getLog()
                .info("message has been posted by '{}': {}", message.screenName, message.message);
        return Behaviors.stopped();
    }
}
