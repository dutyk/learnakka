package chapter14.StyleGuide.FunctionalVersusObjectOrientedStyle;

import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class CounterFunctionalTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCounter() {
        ActorRef<CounterFunctional.Command> counter = testKit.spawn(CounterFunctional.create());
        counter.tell(CounterFunctional.Increment.INSTANCE);

        TestProbe<CounterFunctional.Value> testProbe = testKit.createTestProbe(CounterFunctional.Value.class);
        counter.tell(new CounterFunctional.GetValue(testProbe.getRef()));

        CounterFunctional.Value value = testProbe.receiveMessage();

        Assert.assertEquals(value.value, 1);
    }

    @Test
    public void testCounter1() {
        ActorRef<CounterObjectOriented.Command> counter = testKit.spawn(CounterObjectOriented.create());
        counter.tell(CounterObjectOriented.Increment.INSTANCE);

        TestProbe<CounterObjectOriented.Value> testProbe = testKit.createTestProbe(CounterObjectOriented.Value.class);
        counter.tell(new CounterObjectOriented.GetValue(testProbe.getRef()));

        CounterObjectOriented.Value value = testProbe.receiveMessage();

        Assert.assertEquals(value.value, 1);
    }
}
