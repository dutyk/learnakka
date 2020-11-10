package chapter03.InteractionPatterns.RespondingToAShardedActor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Root actor bootstrapping the application
 */
final class Guardian {

  public static Behavior<Void> create() {
    return Behaviors.setup(context -> {
      CounterConsumer.initSharding(context.getSystem());
      return Behaviors.empty();
    });
  }
}
