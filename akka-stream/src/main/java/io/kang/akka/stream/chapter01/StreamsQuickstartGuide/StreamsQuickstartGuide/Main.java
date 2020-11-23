package io.kang.akka.stream.chapter01.StreamsQuickstartGuide.StreamsQuickstartGuide;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class Main {
    public static CompletionStage<IOResult> streamToFile(Source<Integer, NotUsed> source, ActorSystem system) {
        final Source<BigInteger, NotUsed> factorials =
                source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));

        final CompletionStage<IOResult> result =
                factorials
                        .map(num -> ByteString.fromString(num.toString() + "\n"))
                        .runWith(FileIO.toPath(Paths.get("factorials.txt")), system);
        return result;
    }

    public static CompletionStage<Done> streamToConsole(Source<Integer, NotUsed> source, ActorSystem system) {
        final CompletionStage<Done> done = source.runForeach(i -> System.out.println(i), system);

        return done;
    }

    public static Sink<String, CompletionStage<IOResult>> lineSink(String filename) {
        return Flow.of(String.class)
                .map(s -> ByteString.fromString(s.toString() + "\n"))
                .toMat(FileIO.toPath(Paths.get(filename)), Keep.right());
    }

    public static CompletionStage<IOResult> streamToFile1(Source<Integer, NotUsed> source, ActorSystem system) {
        final Source<BigInteger, NotUsed> factorials =
                source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));

        final CompletionStage<IOResult> result = factorials.map(BigInteger::toString).runWith(lineSink("factorial2.txt"), system);

        return result;
    }

    public static CompletionStage<Done> streamToFile2(Source<Integer, NotUsed> source, ActorSystem system) {
        final Source<BigInteger, NotUsed> factorials =
                source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));

        final CompletionStage<Done> result = factorials
                .zipWith(Source.range(0, 10), (num, idx) -> String.format("%d! = %s", idx, num))
                .throttle(1, Duration.ofSeconds(1))
                .runForeach(s -> System.out.println(s), system);


        return result;
    }


    public static void main(String[] argv) {
        final ActorSystem system = ActorSystem.create("QuickStart");
        final Source<Integer, NotUsed> source = Source.range(1, 100);

        CompletionStage<Done> done = streamToConsole(source, system);

        CompletionStage<IOResult> result = streamToFile(source, system);

        CompletionStage<IOResult> result1 = streamToFile1(source, system);

        CompletionStage<Done> result2 = streamToFile2(source, system);



        done.thenCombine(result, (don, ioResult) -> don)
                .thenCombine(result1, (a, b) -> a)
                .thenCombine(result2, (c, d) -> system.terminate());
    }
}
