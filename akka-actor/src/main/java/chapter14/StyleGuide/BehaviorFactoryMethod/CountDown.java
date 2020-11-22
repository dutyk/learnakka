package chapter14.StyleGuide.BehaviorFactoryMethod;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class CountDown extends AbstractBehavior<CountDown.Command> {

    public interface Command {}

    public enum Down implements Command {
        INSTANCE
    }

    // factory for the initial `Behavior`
    public static Behavior<Command> create(int countDownFrom, ActorRef<Done> notifyWhenZero) {
        return Behaviors.setup(context -> new CountDown(context, countDownFrom, notifyWhenZero));
    }

    private final ActorRef<Done> notifyWhenZero;
    private int remaining;

    private CountDown(
            ActorContext<Command> context, int countDownFrom, ActorRef<Done> notifyWhenZero) {
        super(context);
        this.remaining = countDownFrom;
        this.notifyWhenZero = notifyWhenZero;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(Down.class, notUsed -> onDown()).build();
    }

    private Behavior<Command> onDown() {
        remaining--;
        if (remaining == 0) {
            notifyWhenZero.tell(Done.getInstance());
            return Behaviors.stopped();
        } else {
            return this;
        }
    }
}
