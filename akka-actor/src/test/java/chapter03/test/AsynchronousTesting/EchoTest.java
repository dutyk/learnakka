package chapter03.test.AsynchronousTesting;


import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.event.Logging;
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
        ActorRef<Echo.Ping> pinger = testKit.spawn(Echo.create(), "ping");
        TestProbe<Echo.Pong> probe = testKit.createTestProbe();
        pinger.tell(new Echo.Ping("hello", probe.ref()));
        probe.expectMessage(new Echo.Pong("hello"));

        ActorRef<Echo.Ping> pinger2 = testKit.spawn(Echo.create(), "pinger");
        pinger2.tell(new Echo.Ping("hello", probe.ref()));
        probe.expectMessage(new Echo.Pong("hello"));
        testKit.stop(pinger2, Duration.ofSeconds(10));
    }

    @Test
    public void testLogging() {
        //todo
        ActorRef<String> myLoggingBehavior = testKit.spawn(LoggingDocExamples.MyLoggingBehavior.create());
        TestProbe<LoggingDocExamples.Message> probe = testKit.createTestProbe();
        LoggingDocExamples.logging(probe.getRef());

        TestProbe<String> probe1 = testKit.createTestProbe();
        myLoggingBehavior.tell("hello");
        probe1.expectNoMessage();
    }

}
