package chapter12.test.AsynchronousTesting;


import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import org.junit.Test;

import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;

public class ProducerTest {
    static final ActorTestKit testKit = ActorTestKit.create();

    @Test
    public void testProducer() {
        Behavior<Message> mockedBehavior =
                Behaviors.receiveMessage(
                        message -> {
                            message.replyTo.tell(message.i);
                            return Behaviors.same();
                        });
        TestProbe<Message> probe = testKit.createTestProbe();
        ActorRef<Message> mockedPublisher =
                testKit.spawn(Behaviors.monitor(Message.class, probe.ref(), mockedBehavior));

        // test our component
        Producer producer = new Producer(testKit.scheduler(), mockedPublisher);
        int messages = 3;
        producer.produce(messages);


        // verify expected behavior
        IntStream.range(0, messages)
                .forEach(
                        i -> {
                            Message msg = probe.expectMessageClass(Message.class);
                            assertEquals(i, msg.i);
                        });
    }

}
