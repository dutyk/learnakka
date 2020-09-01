package chapter01.objectorientedstyle;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
    static interface RoomCommand {}

    public static final class GetSession implements RoomCommand {
        public final String screenName;
        public final ActorRef<SessionEvent> replyTo;

        public GetSession(String screenName, ActorRef<SessionEvent> replyTo) {
            this.screenName = screenName;
            this.replyTo = replyTo;
        }
    }

    static interface SessionEvent {}

    public static final class SessionGranted implements SessionEvent {
        public final ActorRef<PostMessage> handle;

        public SessionGranted(ActorRef<PostMessage> handle) {
            this.handle = handle;
        }
    }

    public static final class SessionDenied implements SessionEvent {
        public final String reason;

        public SessionDenied(String reason) {
            this.reason = reason;
        }
    }

    public static final class MessagePosted implements SessionEvent {
        public final String screenName;
        public final String message;

        public MessagePosted(String screenName, String message) {
            this.screenName = screenName;
            this.message = message;
        }
    }

    static interface SessionCommand {}

    public static final class PostMessage implements SessionCommand {
        public final String message;

        public PostMessage(String message) {
            this.message = message;
        }
    }

    private static final class NotifyClient implements SessionCommand {
        final MessagePosted message;

        NotifyClient(MessagePosted message) {
            this.message = message;
        }
    }
    private static final class PublishSessionMessage implements RoomCommand {
        public final String screenName;
        public final String message;

        public PublishSessionMessage(String screenName, String message) {
            this.screenName = screenName;
            this.message = message;
        }
    }

    public static Behavior<RoomCommand> create() {
        return Behaviors.setup(ChatRoomBehavior::new);
    }

    public static class ChatRoomBehavior extends AbstractBehavior<RoomCommand> {
        final List<ActorRef<SessionCommand>> sessions = new ArrayList<>();

        private ChatRoomBehavior(ActorContext<RoomCommand> context) {
            super(context);
        }

        @Override
        public Receive<RoomCommand> createReceive() {
            ReceiveBuilder<RoomCommand> builder = newReceiveBuilder();

            builder.onMessage(GetSession.class, this::onGetSession);
            builder.onMessage(PublishSessionMessage.class, this::onPublishSessionMessage);

            return builder.build();
        }

        private Behavior<RoomCommand> onGetSession(GetSession getSession)
                throws UnsupportedEncodingException {
            ActorRef<SessionEvent> client = getSession.replyTo;
            ActorRef<SessionCommand> ses =
                    getContext()
                            .spawn(
                                    SessionBehavior.create(getContext().getSelf(), getSession.screenName, client),
                                    URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name()));
            // narrow to only expose PostMessage
            client.tell(new SessionGranted(ses.narrow()));
            sessions.add(ses);
            return this;
        }

        private Behavior<RoomCommand> onPublishSessionMessage(PublishSessionMessage pub) {
            NotifyClient notification =
                    new NotifyClient((new MessagePosted(pub.screenName, pub.message)));
            sessions.forEach(s -> s.tell(notification));
            return this;
        }
    }

    static class SessionBehavior extends AbstractBehavior<ChatRoom.SessionCommand> {
        private final ActorRef<RoomCommand> room;
        private final String screenName;
        private final ActorRef<SessionEvent> client;

        public static Behavior<ChatRoom.SessionCommand> create(
                ActorRef<RoomCommand> room, String screenName, ActorRef<SessionEvent> client) {
            return Behaviors.setup(context -> new SessionBehavior(context, room, screenName, client));
        }

        private SessionBehavior(
                ActorContext<ChatRoom.SessionCommand> context,
                ActorRef<RoomCommand> room,
                String screenName,
                ActorRef<SessionEvent> client) {
            super(context);
            this.room = room;
            this.screenName = screenName;
            this.client = client;
        }

        @Override
        public Receive<SessionCommand> createReceive() {
            return newReceiveBuilder()
                    .onMessage(PostMessage.class, this::onPostMessage)
                    .onMessage(NotifyClient.class, this::onNotifyClient)
                    .build();
        }

        private Behavior<SessionCommand> onPostMessage(PostMessage post) {
            // from client, publish to others via the room
            room.tell(new PublishSessionMessage(screenName, post.message));
            return Behaviors.same();
        }

        private Behavior<SessionCommand> onNotifyClient(NotifyClient notification) {
            // published from the room
            client.tell(notification.message);
            return Behaviors.same();
        }
    }
}
