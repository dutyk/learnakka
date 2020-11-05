package chapter03.InteractionPatterns.PerSessionChildActor;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import chapter03.InteractionPatterns.PerSessionChildActor.Home.*;
import static junit.framework.TestCase.assertEquals;

public class HomeTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testLeaveHome() {
        ActorRef<Command> homeActor = testKit.spawn(Home.create());
        TestProbe<ReadyToLeaveHome> leaveHomeTestProbe = testKit.createTestProbe(ReadyToLeaveHome.class);

        homeActor.tell(new LeaveHome("yuan", leaveHomeTestProbe.getRef()));

        ReadyToLeaveHome response = leaveHomeTestProbe.receiveMessage();

        assertEquals(response.who, "yuan");
    }
}
