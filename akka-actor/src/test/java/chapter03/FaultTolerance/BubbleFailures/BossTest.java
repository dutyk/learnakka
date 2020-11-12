package chapter03.FaultTolerance.BubbleFailures;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class BossTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testBoss() {
        ActorRef<Protocol.Command> bossActor = testKit.spawn(Boss.create());
        bossActor.tell(new Protocol.Fail("hello"));
    }
}
