package chapter01;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import static junit.framework.TestCase.assertEquals;

public class HelloWorldMainTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testHelloWorldMain() {
        ActorRef<HelloWorldMain.SayHello> helloWorldMainActor = testKit.spawn(HelloWorldMain.create());
        helloWorldMainActor.tell(new HelloWorldMain.SayHello("yuan-kang"));
    }

    @Test
    public void testHelloWorld() {
        TestProbe<HelloWorld.Greeted> probe =
                testKit.createTestProbe(HelloWorld.Greeted.class);

        ActorRef<HelloWorld.Greet> helloWorldActor = testKit.spawn(HelloWorld.create());
        helloWorldActor.tell(new HelloWorld.Greet("yuankang", probe.getRef()));

        HelloWorld.Greeted greeted = probe.receiveMessage();

        assertEquals(greeted.whom, "yuankang");
    }

}
