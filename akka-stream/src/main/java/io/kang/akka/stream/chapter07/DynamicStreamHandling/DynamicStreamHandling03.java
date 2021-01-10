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

public class DynamicStreamHandling03 {
    //todo eror
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final ActorSystem system = ActorSystem.create("DynamicStreamHandling");

        final Source<Integer, NotUsed> countingSrc =
                Source.from(new ArrayList<>(Arrays.asList(1, 2, 3, 4)))
                        .delay(Duration.ofSeconds(1), DelayOverflowStrategy.backpressure());
        final Sink<Integer, CompletionStage<Integer>> lastSnk = Sink.last();
        final SharedKillSwitch killSwitch = KillSwitches.shared("my-kill-switch");

        final CompletionStage<Integer> completionStage =
                countingSrc
                        .viaMat(killSwitch.flow(), Keep.right())
                        .toMat(lastSnk, Keep.right())
                        .run(system);
        final CompletionStage<Integer> completionStageDelayed =
                countingSrc
                        .delay(Duration.ofSeconds(1), DelayOverflowStrategy.backpressure())
                        .viaMat(killSwitch.flow(), Keep.right())
                        .toMat(lastSnk, Keep.right())
                        .run(system);

        doSomethingElse();
        killSwitch.shutdown();

        final int finalCount = completionStage.toCompletableFuture().get(1, TimeUnit.SECONDS);
        final int finalCountDelayed =
                completionStageDelayed.toCompletableFuture().get(1, TimeUnit.SECONDS);

        System.out.println(finalCount);
        System.out.println(finalCountDelayed);
    }

    private static void doSomethingElse() {}
}
