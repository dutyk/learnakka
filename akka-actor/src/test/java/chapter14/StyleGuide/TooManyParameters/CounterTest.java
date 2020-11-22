package chapter14.StyleGuide.TooManyParameters;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Duration;

public class CounterTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCounter() throws InterruptedException {
        ActorRef<Counter.Command> counter = testKit.spawn(Counter.create("aa"));

        counter.tell(new Counter.IncrementRepeatedly(Duration.ofSeconds(1)));

        TestProbe<Counter.Value> valueTestProbe = testKit.createTestProbe(Counter.Value.class);

        Thread.sleep(2000);

        counter.tell(new Counter.GetValue(valueTestProbe.getRef()));

        Counter.Value value = valueTestProbe.receiveMessage();

        Assert.assertEquals(value.value, 1);
    }

    @Test
    public void testCounter1() throws InterruptedException {
        ActorRef<Counter.Command> counter = testKit.spawn(Counter1.create("aa"));

        counter.tell(new Counter.IncrementRepeatedly(Duration.ofSeconds(1)));

        TestProbe<Counter.Value> valueTestProbe = testKit.createTestProbe(Counter.Value.class);

        Thread.sleep(2000);

        counter.tell(new Counter.GetValue(valueTestProbe.getRef()));

        Counter.Value value = valueTestProbe.receiveMessage();

        Assert.assertEquals(value.value, 1);
    }

    @Test
    public void testCounter2() throws InterruptedException {
        ActorRef<Counter.Command> counter = testKit.spawn(Counter2.create("aa"));

        counter.tell(new Counter.IncrementRepeatedly(Duration.ofSeconds(1)));

        TestProbe<Counter.Value> valueTestProbe = testKit.createTestProbe(Counter.Value.class);

        Thread.sleep(4000);

        counter.tell(new Counter.GetValue(valueTestProbe.getRef()));

        Counter.Value value = valueTestProbe.receiveMessage();

        Assert.assertEquals(value.value, 3);
    }

}
