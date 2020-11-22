package chapter12.test.AsynchronousTesting;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class JunitIntegrationExampleTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();


    @Test
    public void testSomething() {
        ActorRef<Echo.Ping> pinger = testKit.spawn(Echo.create(), "ping");
        TestProbe<Echo.Pong> probe = testKit.createTestProbe();
        pinger.tell(new Echo.Ping("hello", probe.ref()));
        probe.expectMessage(new Echo.Pong("hello"));
    }
}
