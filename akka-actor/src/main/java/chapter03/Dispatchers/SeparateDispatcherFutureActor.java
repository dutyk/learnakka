package chapter03.Dispatchers;

import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SeparateDispatcherFutureActor extends AbstractBehavior<Integer> {
    private final Executor ec;

    public static Behavior<Integer> create() {
        return Behaviors.setup(SeparateDispatcherFutureActor::new);
    }

    private SeparateDispatcherFutureActor(ActorContext<Integer> context) {
        super(context);
        ec =
                context
                        .getSystem()
                        .dispatchers()
                        .lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"));
    }

    @Override
    public Receive<Integer> createReceive() {
        return newReceiveBuilder()
                .onMessage(
                        Integer.class,
                        i -> {
                            triggerFutureBlockingOperation(i, ec);
                            return Behaviors.same();
                        })
                .build();
    }

    private static void triggerFutureBlockingOperation(Integer i, Executor ec) {
        System.out.println("Calling blocking Future on separate dispatcher: " + i);
        CompletableFuture<Integer> f =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                Thread.sleep(5000);
                                System.out.println("Blocking future finished: " + i);
                                return i;
                            } catch (InterruptedException e) {
                                return -1;
                            }
                        },
                        ec);
    }
}