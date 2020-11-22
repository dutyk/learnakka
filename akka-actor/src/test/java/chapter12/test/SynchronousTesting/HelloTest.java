package chapter12.test.SynchronousTesting;


import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import org.junit.Test;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;

public class HelloTest {
    @Test
    public void testHello() {
        BehaviorTestKit<Hello.Command> test = BehaviorTestKit.create(Hello.create());
        TestInbox<String> inbox = TestInbox.create();
        test.run(new Hello.SayHello(inbox.getRef()));
        inbox.expectMessage("hello");
    }

    @Test
    public void testChild() {
        BehaviorTestKit<Hello.Command> testKit = BehaviorTestKit.create(Hello.create());
        testKit.run(new Hello.SayHelloToChild("child"));
        TestInbox<String> childInbox = testKit.childInbox("child");
        childInbox.expectMessage("hello");
    }

    @Test
    public void test() {
        BehaviorTestKit<Hello.Command> testKit = BehaviorTestKit.create(Hello.create());
        testKit.run(Hello.SayHelloToAnonymousChild.INSTANCE);
        // Anonymous actors are created as: $a $b etc
        TestInbox<String> childInbox = testKit.childInbox("$a");
        childInbox.expectMessage("hello stranger");
    }

    @Test
    public void testLog() {
        BehaviorTestKit<Hello.Command> test = BehaviorTestKit.create(Hello.create());
        TestInbox<String> inbox = TestInbox.create("Inboxer");
        test.run(new Hello.LogAndSayHello(inbox.getRef()));

        List<CapturedLogEvent> allLogEntries = test.getAllLogEntries();
        assertEquals(1, allLogEntries.size());
        CapturedLogEvent expectedLogEvent =
                new CapturedLogEvent(
                        Level.INFO,
                        "Saying hello to Inboxer",
                        Optional.empty(),
                        Optional.empty(),
                        new HashMap<>());
        assertEquals(expectedLogEvent, allLogEntries.get(0));
    }
}
