package chapter06.Routers;

import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.PoolRouter;
import akka.actor.typed.javadsl.Routers;

public class PoolRouting {
    public static Behavior<Void> showPoolRouting() {
        return Behaviors.setup(
                context -> {
                    // #pool
                    int poolSize = 4;
                    PoolRouter<Worker.Command> pool =
                            Routers.pool(
                                    poolSize,
                                    // make sure the workers are restarted if they fail
                                    Behaviors.supervise(Worker.create()).onFailure(SupervisorStrategy.restart()));
                    ActorRef<Worker.Command> router = context.spawn(pool, "worker-pool");

                    for (int i = 0; i < 10; i++) {
                        router.tell(new Worker.DoLog("msg " + i));
                    }
                    // #pool

                    // #pool-dispatcher
                    // make sure workers use the default blocking IO dispatcher
                    PoolRouter<Worker.Command> blockingPool =
                            pool.withRouteeProps(DispatcherSelector.blocking());
                    // spawn head router using the same executor as the parent
                    ActorRef<Worker.Command> blockingRouter =
                            context.spawn(blockingPool, "blocking-pool", DispatcherSelector.sameAsParent());
                    // #pool-dispatcher

                    // #strategy
                    PoolRouter<Worker.Command> alternativePool = pool.withPoolSize(2).withRoundRobinRouting();
                    // #strategy

                    return Behaviors.empty();
                });
    }
}
