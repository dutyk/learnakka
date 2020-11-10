package chapter03.InteractionPatterns.RespondingToAShardedActor;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;

public class CounterConsumer extends AbstractBehavior<CounterConsumer.Command> {
    public static EntityTypeKey<Command> typeKey =
            EntityTypeKey.create(Command.class, "example-sharded-response");

    public static void initSharding(ActorSystem<?> system) {
        ClusterSharding.get(system).init(Entity.of(typeKey, entityContext ->
                CounterConsumer.create()
        ));
    }
    private CounterConsumer(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
       return Behaviors.setup(CounterConsumer::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(NewCount.class, this::onNewCount)
                .build();
    }

    private Behavior<Command> onNewCount(NewCount newCount) {
        getContext().getLog().info("{}", newCount.value);
        return this;
    }

    public interface Command {}

    public static class NewCount implements Command {
        public final long value;

        public NewCount(long value) {
            this.value = value;
        }
    }
}

