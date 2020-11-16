package chapter03.InteractionPatterns.RespondingToAShardedActor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Collections;

public class Example {
    public static void main(String[] args) {
        Config config = ConfigFactory.load("application-remote");
        ActorSystem system = ActorSystem.create(Behaviors.empty(), "ClusterSystem", config);

        // #sharding-extension
        ClusterSharding sharding = ClusterSharding.get(system);
        // #sharding-extension

        // #init
        EntityTypeKey<Counter1.Command> typeKey = EntityTypeKey.create(Counter1.Command.class, "Counter");

        ActorRef<ShardingEnvelope<Counter1.Command>> shardRegion =
                sharding.init(Entity.of(typeKey, ctx -> Counter1.create(ctx.getEntityId())));
        // #init

        // #send
        EntityRef<Counter1.Command> counterOne = sharding.entityRefFor(typeKey, "counter-1");
        counterOne.tell(Counter1.Increment.INSTANCE);

        shardRegion.tell(new ShardingEnvelope<>("counter-1", Counter1.Increment.INSTANCE));
        // #send
    }

    private static Config configWithPort(int port) {
        return ConfigFactory.parseMap(
                Collections.singletonMap("akka.remote.artery.canonical.port", Integer.toString(port))
        ).withFallback(ConfigFactory.load("application-remote"));
    }
}
