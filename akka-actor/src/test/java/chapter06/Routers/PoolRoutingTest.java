package chapter06.Routers;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class PoolRoutingTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testPoolRouting() {
        ActorRef<Void> poolRouting = testKit.spawn(PoolRouting.showPoolRouting());
    }

    @Test
    public void testGroupRouting() {
        ActorRef<Void> poolRouting = testKit.spawn(GroupRouting.showGroupRouting());
    }

}
