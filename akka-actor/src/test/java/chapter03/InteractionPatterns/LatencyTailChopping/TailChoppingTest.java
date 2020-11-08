package chapter03.InteractionPatterns.LatencyTailChopping;


import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Duration;
import java.util.function.BiFunction;

public class TailChoppingTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testTailChopping() throws InterruptedException {
        TestProbe<Response> replyToActor = testKit.createTestProbe(Response.class);

        BiFunction<Integer, ActorRef<Response>, Boolean> sendRequest = ((a, b) -> {
            if(a < 4) {
                return true;
            }else {
                return false;
            }
        });

        ActorRef<TailChopping.Command> cookieFabric = testKit.spawn(TailChopping.create(
                Response.class ,sendRequest, Duration.ofSeconds(1), replyToActor.getRef(), Duration.ofSeconds(5), new Response("hello")));
        Thread.sleep(1000 * 10);
    }

}
