package chapter04.FaultTolerance;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class CounterTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCounter() {
        ActorRef<Counter.Command> counterActor = testKit.spawn(Counter.create());
        TestProbe<Counter.Got> gotActor = testKit.createTestProbe(Counter.Got.class);

        counterActor.tell(new Counter.Increase());
        counterActor.tell(new Counter.Increase());

        counterActor.tell(new Counter.Get(gotActor.getRef()));

        Counter.Got gotMsg = gotActor.receiveMessage();

        Assert.assertEquals(gotMsg.n, 1);
    }
}
