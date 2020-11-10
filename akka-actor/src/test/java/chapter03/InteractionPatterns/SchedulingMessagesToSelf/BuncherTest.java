package chapter03.InteractionPatterns.SchedulingMessagesToSelf;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import chapter03.InteractionPatterns.SchedulingMessagesToSelf.Buncher.*;
import java.time.Duration;

public class BuncherTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testBuncher() {
        TestProbe<Batch> batchActor = testKit.createTestProbe(Batch.class);

        ActorRef<Command> buncherActor = testKit.spawn(Buncher.create(batchActor.getRef(), Duration.ofSeconds(5), 3));

        buncherActor.tell(new ExcitingMessage("hello"));
        buncherActor.tell(new ExcitingMessage("hello1"));
        buncherActor.tell(new ExcitingMessage("hello2"));
        buncherActor.tell(new ExcitingMessage("hello3"));
        buncherActor.tell(new ExcitingMessage("hello4"));

        Batch batch = batchActor.receiveMessage(Duration.ofSeconds(10));
        Assert.assertEquals(batch.getMessages().size(),3);
    }

}
