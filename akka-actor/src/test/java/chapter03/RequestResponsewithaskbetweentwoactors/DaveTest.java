package chapter03.RequestResponsewithaskbetweentwoactors;

import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.junit.ClassRule;
import org.junit.Test;

public class DaveTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testDave() {
        ActorRef<Hal.Command> halActor = testKit.spawn(Hal.create());

        ActorRef<Dave.Command> daveActor = testKit.spawn(Dave.create(halActor));
    }

}
