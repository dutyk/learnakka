package chapter09.CoordinatedShutdown;

import akka.Done;
import akka.actor.Cancellable;
import akka.actor.CoordinatedShutdown;
import akka.actor.typed.ActorSystem;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RootMain {
    private CompletionStage<Done> cleanup() {
        System.out.println("cleanUp");
        return CompletableFuture.completedFuture(Done.done());
    }

    public void mount() {
        ActorSystem<Void> system = ActorSystem.create(Root.create(), "main");

        // #coordinated-shutdown-cancellable
        Cancellable cancellable =
                CoordinatedShutdown.get(system)
                        .addCancellableTask(
                                CoordinatedShutdown.PhaseBeforeServiceUnbind(), "someTaskCleanup", () -> cleanup());
        // much later...

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cancellable.cancel();
        // #coordinated-shutdown-cancellable

        // #coordinated-shutdown-jvm-hook
        CoordinatedShutdown.get(system)
                .addJvmShutdownHook(() -> System.out.println("custom JVM shutdown hook..."));
        // #coordinated-shutdown-jvm-hook

        // don't run this
        if (true) {
            // #coordinated-shutdown-run
            // shut down with `ActorSystemTerminateReason`
            system.terminate();

            // or define a specific reason
            class UserInitiatedShutdown implements CoordinatedShutdown.Reason {
                @Override
                public String toString() {
                    return "UserInitiatedShutdown";
                }
            }

            CompletionStage<Done> done =
                    CoordinatedShutdown.get(system).runAll(new UserInitiatedShutdown());
            // #coordinated-shutdown-run
        }
    }

    public static void main(String[] args) {
        RootMain rootMain = new RootMain();
        rootMain.mount();
    }
}
