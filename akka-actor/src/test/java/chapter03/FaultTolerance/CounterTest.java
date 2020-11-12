package chapter03.FaultTolerance;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import chapter03.FaultTolerance.Counter.*;

public class CounterTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCounter() {
        ActorRef<Command> counterActor = testKit.spawn(Counter.create());
        TestProbe<Got> gotActor = testKit.createTestProbe(Got.class);

        counterActor.tell(new Increase());
        counterActor.tell(new Increase());

        counterActor.tell(new Get(gotActor.getRef()));

        Got gotMsg = gotActor.receiveMessage();

        Assert.assertEquals(gotMsg.n, 1);
    }
}
