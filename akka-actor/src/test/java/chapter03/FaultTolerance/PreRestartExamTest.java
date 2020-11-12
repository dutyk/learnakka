package chapter03.FaultTolerance;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class PreRestartExamTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testRestartParent() {
        ActorRef<String> parentActor = testKit.spawn(PreRestartExam.create());

        parentActor.tell("");
    }
}
