package chapter03.AdaptedResponse;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.net.URI;

public class Backend extends AbstractBehavior<Backend.Request> {
    public Backend(ActorContext<Request> context) {
        super(context);
    }

    public static Behavior<Request> create() {
        return Behaviors.setup(context -> new Backend(context));
    }


    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartTranslationJob.class, this::doTranslate)
                .build();
    }

    private Behavior<Request> doTranslate(StartTranslationJob cmd) {
        getContext().getLog().info("{}, {}", cmd.site, cmd.taskId);
        cmd.replyTo.tell(new JobStarted(cmd.taskId));
        return Behaviors.same();
    }

    public interface Request {}

    public static class StartTranslationJob implements Request {
        public final int taskId;
        public final URI site;
        public final ActorRef<Response> replyTo;

        public StartTranslationJob(int taskId, URI site, ActorRef<Response> replyTo) {
            this.taskId = taskId;
            this.site = site;
            this.replyTo = replyTo;
        }
    }

    public interface Response {}

    public static class JobStarted implements Response {
        public final int taskId;

        public JobStarted(int taskId) {
            this.taskId = taskId;
        }
    }

    public static class JobProgress implements Response {
        public final int taskId;
        public final double progress;

        public JobProgress(int taskId, double progress) {
            this.taskId = taskId;
            this.progress = progress;
        }
    }

    public static class JobCompleted implements Response {
        public final int taskId;
        public final URI result;

        public JobCompleted(int taskId, URI result) {
            this.taskId = taskId;
            this.result = result;
        }
    }
}

