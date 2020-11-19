package chapter03.test.AsynchronousTesting;

import akka.actor.testkit.typed.javadsl.LogCapturing;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class LogCapturingExampleTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Rule
    public final LogCapturing logCapturing = new LogCapturing();

    @Test
    public void testSomething() {
        ActorRef<Echo.Ping> pinger = testKit.spawn(Echo.create(), "ping");
        TestProbe<Echo.Pong> probe = testKit.createTestProbe();
        pinger.tell(new Echo.Ping("hello", probe.ref()));
        probe.expectMessage(new Echo.Pong("hello"));
    }
}
