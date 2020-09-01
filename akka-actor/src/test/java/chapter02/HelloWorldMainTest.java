package chapter02;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class HelloWorldMainTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testHelloWorldMain() {
        ActorRef<HelloWorldMain.SayHello> helloWorldMainActor = testKit.spawn(HelloWorldMain.create());
        helloWorldMainActor.tell(new HelloWorldMain.SayHello("yuan-kang"));
    }
}
