package chapter03.IgnoringReplies;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import chapter03.requestresponse.CookieFabric;
import org.junit.ClassRule;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class CookieFabricTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCookieFabric() {
        ActorRef<CookieFabric.Request> cookieFabric = testKit.spawn(CookieFabric.create());
        TestProbe<CookieFabric.Response> responseTestProbe = testKit.createTestProbe(CookieFabric.Response.class);

        cookieFabric.tell(new CookieFabric.Request("give me cookies", responseTestProbe.getRef()));

        CookieFabric.Response response = responseTestProbe.receiveMessage();
        assertEquals(response.result, "Here are the cookies for give me cookies");

        cookieFabric.tell(new CookieFabric.Request("give me cookies", testKit.system().ignoreRef()));
        responseTestProbe.expectNoMessage();
        // #request-response-send
    }
}
