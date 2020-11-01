package chapter03.GenericResponseWrapper;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class CookieFabricTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCookieFabric() {
        ActorRef<CookieFabric.Command> cookieFabric = testKit.spawn(CookieFabric.create());
        ActorSystem<Void> system = testKit.system();

        CompletionStage<CookieFabric.Cookies> cookies =
                AskPattern.askWithStatus(
                        cookieFabric,
                        replyTo -> new CookieFabric.GiveMeCookies(3, replyTo),
                        Duration.ofSeconds(3),
                        system.scheduler());

        cookies.whenComplete(
                (cookiesReply, failure) -> {
                    if (cookies != null) System.out.println("Yay, " + cookiesReply.count + " cookies!");
                    else System.out.println("Boo! didn't get cookies in time. " + failure);
                });
    }
}
