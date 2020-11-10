package chapter03.InteractionPatterns.RespondingToAShardedActor;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import chapter03.InteractionPatterns.RespondingToAShardedActor.Counter.*;

public class CounterTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    //todo not correct
    @Test
    public void testSharding() {
        ActorRef<Command> counterActor = testKit.spawn(Counter.create());

        counterActor.tell(Increment.INSTANCE);

        counterActor.tell(new GetValue("example-sharded-response"));

        TestProbe<CounterConsumer.NewCount> counterCousumerActor = testKit.createTestProbe(CounterConsumer.NewCount.class);
        CounterConsumer.NewCount result = counterCousumerActor.receiveMessage();
        System.out.println(result.value);
    }

}
