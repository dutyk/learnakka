package io.kang.akka.stream.chapter06;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.util.Arrays;

public class Buffers01 {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Buffers");

        Source.from(Arrays.asList(1, 2, 3))
                .map(
                        i -> {
                            System.out.println("A: " + i);
                            return i;
                        })
                .async()
                .map(
                        i -> {
                            System.out.println("B: " + i);
                            return i;
                        })
                .async()
                .map(
                        i -> {
                            System.out.println("C: " + i);
                            return i;
                        })
                .async()
                .runWith(Sink.ignore(), system);
    }
}
