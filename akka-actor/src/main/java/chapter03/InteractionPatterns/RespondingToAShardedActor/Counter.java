package chapter03.InteractionPatterns.RespondingToAShardedActor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;

// a sharded counter that sends responses to another sharded actor
public class Counter extends AbstractBehavior<Counter.Command> {

    public interface Command {}

    public enum Increment implements Command {
        INSTANCE
    }

    public static class GetValue implements Command {
        public final String replyToEntityId;

        public GetValue(String replyToEntityId) {
            this.replyToEntityId = replyToEntityId;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Counter::new);
    }

    private final ClusterSharding sharding;
    private int value = 0;

    private Counter(ActorContext<Command> context) {
        super(context);
        this.sharding = ClusterSharding.get(context.getSystem());
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Increment.class, msg -> onIncrement())
                .onMessage(GetValue.class, this::onGetValue)
                .build();
    }

    private Behavior<Command> onIncrement() {
        value++;
        getContext().getLog().info("+++++++++++++++++++");
        return this;
    }

    private Behavior<Command> onGetValue(GetValue msg) {
        EntityRef<CounterConsumer.Command> entityRef =
                sharding.entityRefFor(CounterConsumer.typeKey, msg.replyToEntityId);
        entityRef.tell(new CounterConsumer.NewCount(value));
        return this;
    }
}
