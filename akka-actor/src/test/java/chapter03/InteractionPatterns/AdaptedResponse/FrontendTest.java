package chapter03.InteractionPatterns.AdaptedResponse;


import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import org.junit.ClassRule;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class FrontendTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testFront() throws URISyntaxException {
        ActorRef<Backend.Request> backend = testKit.spawn(Backend.create());
        TestProbe<URI> uri = testKit.createTestProbe(URI.class);

        ActorRef<Frontend.Command> translator = testKit.spawn(Frontend.Translator.create(backend));

        translator.tell(new Frontend.Translate(new URI("www.baidu.com"), uri.getRef()));
    }

}
