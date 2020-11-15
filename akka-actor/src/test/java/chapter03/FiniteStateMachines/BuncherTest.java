package chapter03.FiniteStateMachines;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import java.util.Arrays;

public class BuncherTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testBuncher() {
        ActorRef<Buncher.Event> buncherActor = testKit.spawn(Buncher.create());
        TestProbe<Buncher.Batch> batchActor = testKit.createTestProbe(Buncher.Batch.class);

        buncherActor.tell(new Buncher.SetTarget(batchActor.getRef()));
        buncherActor.tell(new Buncher.Queue("aaa"));
        buncherActor.tell(Buncher.Flush.INSTANCE);
        Buncher.Batch batch = batchActor.receiveMessage();
        System.out.println(Arrays.toString(batch.list.toArray()));

        buncherActor.tell(new Buncher.Queue("bbb"));

        batch = batchActor.receiveMessage();
        System.out.println(Arrays.toString(batch.list.toArray()));

    }

}
