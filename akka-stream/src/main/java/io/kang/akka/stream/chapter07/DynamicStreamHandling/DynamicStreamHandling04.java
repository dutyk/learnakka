package io.kang.akka.stream.chapter07.DynamicStreamHandling;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.DelayOverflowStrategy;
import akka.stream.KillSwitches;
import akka.stream.SharedKillSwitch;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DynamicStreamHandling04 {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final ActorSystem system = ActorSystem.create("DynamicStreamHandling");

        final Source<Integer, NotUsed> countingSrc =
                Source.from(new ArrayList<>(Arrays.asList(1, 2, 3, 4)))
                        .delay(Duration.ofSeconds(1), DelayOverflowStrategy.backpressure());
        final Sink<Integer, CompletionStage<Integer>> lastSnk = Sink.last();
        final SharedKillSwitch killSwitch = KillSwitches.shared("my-kill-switch");

        final CompletionStage<Integer> completionStage1 =
                countingSrc
                        .viaMat(killSwitch.flow(), Keep.right())
                        .toMat(lastSnk, Keep.right())
                        .run(system);
        final CompletionStage<Integer> completionStage2 =
                countingSrc
                        .viaMat(killSwitch.flow(), Keep.right())
                        .toMat(lastSnk, Keep.right())
                        .run(system);

        final Exception error = new Exception("boom!");
        killSwitch.abort(error);

        final int result1 =
                completionStage1.toCompletableFuture().exceptionally(e -> -1).get(1, TimeUnit.SECONDS);
        final int result2 =
                completionStage2.toCompletableFuture().exceptionally(e -> -1).get(1, TimeUnit.SECONDS);

        System.out.println(result1);
        System.out.println(result2);
    }

    private static void doSomethingElse() {}
}
