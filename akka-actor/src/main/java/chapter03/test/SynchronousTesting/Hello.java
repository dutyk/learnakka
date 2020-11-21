package chapter03.test.SynchronousTesting;
import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

public class Hello extends AbstractBehavior<Hello.Command> {

    public interface Command {}

    public static class CreateAChild implements Command {
        public final String childName;

        public CreateAChild(String childName) {
            this.childName = childName;
        }
    }

    public enum CreateAnAnonymousChild implements Command {
        INSTANCE
    }

    public static class SayHelloToChild implements Command {
        public final String childName;

        public SayHelloToChild(String childName) {
            this.childName = childName;
        }
    }

    public enum SayHelloToAnonymousChild implements Command {
        INSTANCE
    }

    public static class SayHello implements Command {
        public final ActorRef<String> who;

        public SayHello(ActorRef<String> who) {
            this.who = who;
        }
    }

    public static class LogAndSayHello implements Command {
        public final ActorRef<String> who;

        public LogAndSayHello(ActorRef<String> who) {
            this.who = who;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Hello::new);
    }

    private Hello(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CreateAChild.class, this::onCreateAChild)
                .onMessage(CreateAnAnonymousChild.class, this::onCreateAnonymousChild)
                .onMessage(SayHelloToChild.class, this::onSayHelloToChild)
                .onMessage(SayHelloToAnonymousChild.class, this::onSayHelloToAnonymousChild)
                .onMessage(SayHello.class, this::onSayHello)
                .onMessage(LogAndSayHello.class, this::onLogAndSayHello)
                .build();
    }

    private Behavior<Command> onCreateAChild(CreateAChild message) {
        getContext().spawn(Child.create(), message.childName);
        return Behaviors.same();
    }

    private Behavior<Command> onCreateAnonymousChild(CreateAnAnonymousChild message) {
        getContext().spawnAnonymous(Child.create());
        return Behaviors.same();
    }

    private Behavior<Command> onSayHelloToChild(SayHelloToChild message) {
        ActorRef<String> child = getContext().spawn(Child.create(), message.childName);
        child.tell("hello");
        return Behaviors.same();
    }

    private Behavior<Command> onSayHelloToAnonymousChild(SayHelloToAnonymousChild message) {
        ActorRef<String> child = getContext().spawnAnonymous(Child.create());
        child.tell("hello stranger");
        return Behaviors.same();
    }

    private Behavior<Command> onSayHello(SayHello message) {
        message.who.tell("hello");
        return Behaviors.same();
    }

    private Behavior<Command> onLogAndSayHello(LogAndSayHello message) {
        getContext().getLog().info("Saying hello to {}", message.who.path().name());
        message.who.tell("hello");
        return Behaviors.same();
    }
}
