package chapter07.Stash;


import akka.Done;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class DataAccessTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testDataAccess() {
        ActorRef<DataAccess.Command> dataAccessActor = testKit.spawn(DataAccess.create("1", new DBImpl()));
        TestProbe<Done> doneActorRef = testKit.createTestProbe(Done.class);
        TestProbe<String> replyActorRef = testKit.createTestProbe(String.class);

        dataAccessActor.tell(new DataAccess.Save("yuan", doneActorRef.getRef()));

        dataAccessActor.tell(new DataAccess.Get(replyActorRef.getRef()));
        String msg = replyActorRef.receiveMessage();
        System.out.println(msg);
    }

}
