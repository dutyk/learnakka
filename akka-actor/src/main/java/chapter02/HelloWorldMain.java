package chapter02;

import akka.actor.typed.Props;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import chapter01.HelloWorld;
import chapter01.HelloWorldBot;

public class HelloWorldMain extends AbstractBehavior<HelloWorldMain.SayHello> {
    public static class SayHello {
        public final String name;

        public SayHello(String name) {
            this.name = name;
        }
    }

    public static Behavior<SayHello> create() {
        return Behaviors.setup(HelloWorldMain::new);
    }

    private final ActorRef<HelloWorld.Greet> greeter;

    private HelloWorldMain(ActorContext<SayHello> context) {
        super(context);
        final String dispatcherPath = "akka.actor.default-blocking-io-dispatcher";
        Props greeterProps = DispatcherSelector.fromConfig(dispatcherPath);
        greeter = getContext().spawn(HelloWorld.create(), "greeter", greeterProps);
    }

    @Override
    public Receive<SayHello> createReceive() {
        return newReceiveBuilder().onMessage(SayHello.class, this::onStart).build();
    }

    private Behavior<SayHello> onStart(SayHello command) {
        ActorRef<HelloWorld.Greeted> replyTo =
                getContext().spawn(HelloWorldBot.create(3), command.name);
        greeter.tell(new HelloWorld.Greet(command.name, replyTo));
        return this;
    }
}
