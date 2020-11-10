package chapter03.InteractionPatterns.RespondingToAShardedActor;

import akka.actor.typed.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.Collections;


public class CountApp {
    public static void main(String[] args) {
        Config config = configWithPort(2551);
        ActorSystem counterConsumerActor = ActorSystem.create(Guardian.create(), "ClusterSystem", config);

//        config = configWithPort(2552);
//        ActorSystem counterConsumerActor1 = ActorSystem.create(Guardian.create(), "ClusterSystem", config);

        config = configWithPort(0);
        ActorSystem counterActor = ActorSystem.create(Counter.create(), "ClusterSystem", config);

        counterActor.tell(Counter.Increment.INSTANCE);
        counterActor.tell(Counter.Increment.INSTANCE);
        counterActor.tell(Counter.Increment.INSTANCE);
        //counterActor.tell(new Counter.GetValue("example-sharded-response"));

    }
    private static Config configWithPort(int port) {
        return ConfigFactory.parseMap(
                Collections.singletonMap("akka.remote.artery.canonical.port", Integer.toString(port))
        ).withFallback(ConfigFactory.load());
    }
}
