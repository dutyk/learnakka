package chapter14.StyleGuide.BehaviorFactoryMethod;

import akka.Done;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class CountDownTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCountDown() {
        TestProbe<Done> doneProbe = testKit.createTestProbe(Done.class);
        ActorRef<CountDown.Command> coutDown = testKit.spawn(CountDown.create(100, doneProbe.getRef()), "countDown");

        for(int i = 0; i < 100; i++) {
            coutDown.tell(CountDown.Down.INSTANCE);
        }

        Done done = doneProbe.receiveMessage();

        System.out.println(done);
    }

}
