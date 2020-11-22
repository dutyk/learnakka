package chapter10.Dispatchers;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class AppNoBlocking {
    public static void main(String[] args) {
        Behavior<Void> root =
                Behaviors.setup(
                        context -> {
                            for (int i = 0; i < 100; i++) {
                                context.spawn(SeparateDispatcherFutureActor.create(), "NoBlockingActor-" + i).tell(i);
                                context.spawn(PrintActor.create(), "PrintActor-" + i).tell(i);
                            }
                            return Behaviors.ignore();
                        });

        Config config = ConfigFactory.load("application-dispatcher");
        ActorSystem<Void> system = ActorSystem.<Void>create(root, "NoBlockingDispatcherTest", config);
    }
}
