package io.kang.akka.stream.chapter04.graph;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class GraphAppTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("StreamPartialGraphDSLDocTest");
    }

    @AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public  void demonstrateBuildSinkWithCombine() throws Exception {
        final TestKit probe = new TestKit(system);
        ActorRef actorRef = probe.getRef();

        // #sink-combine
        Sink<Integer, NotUsed> sendRemotely = Sink.actorRef(actorRef, "Done");

        Sink<Integer, CompletionStage<Done>> localProcessing =
                Sink.<Integer>foreach(
                        a -> {
                            System.out.println(a);
                        });
        Sink<Integer, NotUsed> sinks =
                Sink.combine(sendRemotely, localProcessing, new ArrayList<>(), a -> Broadcast.create(a));

        Source.<Integer>from(Arrays.asList(new Integer[] {0, 1, 2})).runWith(sinks, system);
        // #sink-combine
        probe.expectMsgEquals(0);
        probe.expectMsgEquals(1);
        probe.expectMsgEquals(2);
    }

}
