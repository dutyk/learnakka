package chapter12.test.AsynchronousTesting;


import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.AfterClass;
import org.junit.Test;

import java.time.Duration;

public class EchoTest {
    static final ActorTestKit testKit = ActorTestKit.create();

    @AfterClass
    public static void cleanup() {
        testKit.shutdownTestKit();
    }

    @Test
    public void testEcho() {
        TestProbe<LoggingDocExamples.Message> msgProbe = testKit.createTestProbe();

        ActorRef<Echo.Ping> pinger = testKit.spawn(Echo.create(), "ping");
        TestProbe<Echo.Pong> probe = testKit.createTestProbe();
        pinger.tell(new Echo.Ping("hello", probe.ref()));
        probe.expectMessage(new Echo.Pong("hello"));

        ActorRef<Echo.Ping> pinger2 = testKit.spawn(Echo.create(), "pinger");
        pinger2.tell(new Echo.Ping("hello", probe.ref()));
        probe.expectMessage(new Echo.Pong("hello"));
        testKit.stop(pinger2, Duration.ofSeconds(10));
    }
}
