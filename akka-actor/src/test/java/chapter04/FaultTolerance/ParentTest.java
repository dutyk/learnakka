package chapter04.FaultTolerance;

import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.junit.ClassRule;
import org.junit.Test;

public class ParentTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testRestartParent() {
        ActorRef<String> parentActor = testKit.spawn(Parent.create());

        parentActor.tell("");
    }

}
