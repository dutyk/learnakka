package chapter05.ActorDiscovery;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class GuardianTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testGuardian() {
        ActorRef<Void> guardianActor = testKit.spawn(Guardian.create());
    }
}
