package chapter10.Dispatchers;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class App {
    public static void main(String[] args) {
        Behavior<Void> root =
                Behaviors.setup(
                        context -> {
                            for (int i = 0; i < 100; i++) {
                                context.spawn(BlockingActor.create(), "BlockingActor-" + i).tell(i);
                                context.spawn(PrintActor.create(), "PrintActor-" + i).tell(i);
                            }
                            return Behaviors.ignore();
                        });
        ActorSystem<Void> system = ActorSystem.<Void>create(root, "BlockingDispatcherTest");
    }
}
