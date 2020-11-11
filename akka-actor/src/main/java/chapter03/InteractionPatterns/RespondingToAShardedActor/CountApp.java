package chapter03.InteractionPatterns.RespondingToAShardedActor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;

public class CountApp {
    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create(Behaviors.empty(), "ClusterSystem");

        // #sharding-extension
        ClusterSharding sharding = ClusterSharding.get(system);

        ActorRef<ShardingEnvelope<Counter.Command>> shardRegion =
                sharding.init(Entity.of(Counter.typeKey, ctx -> Counter.create()));

        ActorRef<ShardingEnvelope<CounterConsumer.Command>> shardRegion1 =
                sharding.init(Entity.of(CounterConsumer.typeKey, ctx -> CounterConsumer.create()));

        EntityRef<Counter.Command> counterActor = sharding.entityRefFor(Counter.typeKey, "counter-1");
        counterActor.tell(Counter.Increment.INSTANCE);
        counterActor.tell(new Counter.GetValue("counterConsumer-1"));
    }
}
