package chapter01.objectorientedstyle;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Gabbler extends AbstractBehavior<ChatRoom.SessionEvent> {
    public static Behavior<ChatRoom.SessionEvent> create() {
        return Behaviors.setup(Gabbler::new);
    }

    private Gabbler(ActorContext<ChatRoom.SessionEvent> context) {
        super(context);
    }

    @Override
    public Receive<ChatRoom.SessionEvent> createReceive() {
        ReceiveBuilder<ChatRoom.SessionEvent> builder = newReceiveBuilder();
        return builder
                .onMessage(ChatRoom.SessionDenied.class, this::onSessionDenied)
                .onMessage(ChatRoom.SessionGranted.class, this::onSessionGranted)
                .onMessage(ChatRoom.MessagePosted.class, this::onMessagePosted)
                .build();
    }

    private Behavior<ChatRoom.SessionEvent> onSessionDenied(ChatRoom.SessionDenied message) {
        getContext().getLog().info("cannot start chat room session: {}", message.reason);
        return Behaviors.stopped();
    }

    private Behavior<ChatRoom.SessionEvent> onSessionGranted(ChatRoom.SessionGranted message) {
        message.handle.tell(new ChatRoom.PostMessage("Hello World!"));
        return Behaviors.same();
    }

    private Behavior<ChatRoom.SessionEvent> onMessagePosted(ChatRoom.MessagePosted message) {
        getContext()
                .getLog()
                .info("message has been posted by '{}': {}", message.screenName, message.message);
        return Behaviors.stopped();
    }
}
