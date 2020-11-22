package chapter03.StyleGuide.PublicVersusPrivateMessages;

import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import java.time.Duration;

public class CounterTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCounter() throws InterruptedException {
        ActorRef<Counter.Command> counterActor = testKit.spawn(Counter.create("aa", Duration.ofSeconds(1)));
        counterActor.tell(Counter.Increment.INSTANCE);

        Thread.sleep(2000);
        TestProbe<Counter.Value> valueTestProbe = testKit.createTestProbe(Counter.Value.class);
        counterActor.tell(new Counter.GetValue(valueTestProbe.getRef()));

        Counter.Value value = valueTestProbe.receiveMessage();
        Assert.assertEquals(value.value, 2);
    }

    @Test
    public void testCounter1() throws InterruptedException {
        ActorRef<Counter1.Command> counterActor = testKit.spawn(Counter1.create("aa", Duration.ofSeconds(1)));
        counterActor.tell(Counter1.Increment.INSTANCE);

        Thread.sleep(2000);
        TestProbe<Counter1.Value> valueTestProbe = testKit.createTestProbe(Counter1.Value.class);
        counterActor.tell(new Counter1.GetValue(valueTestProbe.getRef()));

        Counter1.Value value = valueTestProbe.receiveMessage();
        Assert.assertEquals(value.value, 2);
    }

}
