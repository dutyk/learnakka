package chapter03.RequestResponsewithaskfromoutsideanActor;


import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CookieFabricTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCookieFabric() {
        ActorRef<CookieFabric.Command> cookieFabric = testKit.spawn(CookieFabric.create());
        ActorSystem<Void> system = testKit.system();

        CompletionStage<CookieFabric.Reply> result =
                AskPattern.ask(
                        cookieFabric,
                        replyTo -> new CookieFabric.GiveMeCookies(8, replyTo),
                        // asking someone requires a timeout and a scheduler, if the timeout hits without
                        // response the ask is failed with a TimeoutException
                        Duration.ofSeconds(3),
                        system.scheduler());

        result.whenComplete(
                (reply, failure) -> {
                    if (reply instanceof CookieFabric.Cookies)
                        System.out.println("Yay, " + ((CookieFabric.Cookies) reply).count + " cookies!");
                    else if (reply instanceof CookieFabric.InvalidRequest)
                        System.out.println(
                                "No cookies for me. " + ((CookieFabric.InvalidRequest) reply).reason);
                    else System.out.println("Boo! didn't get cookies in time. " + failure);
                });
    }

    @Test
    public void testCookieFabric1() {
        ActorRef<CookieFabric.Command> cookieFabric = testKit.spawn(CookieFabric.create());
        ActorSystem<Void> system = testKit.system();

        CompletionStage<CookieFabric.Reply> result =
                AskPattern.ask(
                        cookieFabric,
                        replyTo -> new CookieFabric.GiveMeCookies(3, replyTo),
                        Duration.ofSeconds(3),
                        system.scheduler());

        CompletionStage<CookieFabric.Cookies> cookies =
                result.thenCompose(
                        (CookieFabric.Reply reply) -> {
                            if (reply instanceof CookieFabric.Cookies) {
                                return CompletableFuture.completedFuture((CookieFabric.Cookies) reply);
                            } else if (reply instanceof CookieFabric.InvalidRequest) {
                                CompletableFuture<CookieFabric.Cookies> failed = new CompletableFuture<>();

                                failed.completeExceptionally(
                                        new IllegalArgumentException(((CookieFabric.InvalidRequest) reply).reason));
                                return failed;
                            } else {
                                throw new IllegalStateException("Unexpected reply: " + reply.getClass());
                            }
                        });

        cookies.whenComplete(
                (cookiesReply, failure) -> {
                    if (failure == null) System.out.println("Yay, " + cookiesReply.count + " cookies!");
                    else System.out.println("Boo! didn't get cookies in time. " + failure);
                });
    }

}
