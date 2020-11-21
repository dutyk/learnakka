package chapter03.test.AsynchronousTesting;

import akka.actor.testkit.typed.javadsl.LoggingTestKit;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class LoggingTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testLog() {
        ActorRef<Echo.Ping> pinger = testKit.spawn(Echo.create(), "ping");
        TestProbe<Echo.Pong> probe = testKit.createTestProbe();

        LoggingTestKit.info("Received message").expect(
                testKit.system(),
                () -> {
                    System.out.println("------------");
                    pinger.tell(new Echo.Ping("hello", probe.ref()));
                    return null;
                }
        );
        probe.expectMessage(new Echo.Pong("hello"));
    }
}
