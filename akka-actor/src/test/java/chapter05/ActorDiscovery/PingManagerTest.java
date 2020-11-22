package chapter05.ActorDiscovery;

import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.junit.ClassRule;
import org.junit.Test;

public class PingManagerTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testPingManager() {
        ActorRef<PingManager.Command> pingManager = testKit.spawn(PingManager.create());

        pingManager.tell(PingManager.PingAll.INSTANCE);
    }
}
