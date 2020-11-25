package io.kang.akka.stream.chapter03.Flows;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import org.junit.Test;

public class RunWithMyselfTest {

    @Test
    public void testRunWithMyself() {
        ActorSystem system = ActorSystem.create("flows");

        ActorRef runWithMyself = system.actorOf(RunWithMyself.props(), "runWithMyself");

        runWithMyself.tell("hello", ActorRef.noSender());
    }

    @Test
    public void testRunForever() throws InterruptedException {
        ActorSystem system = ActorSystem.create("flows");

        Materializer mat = Materializer.createMaterializer(system);

        ActorRef runWithMyself = system.actorOf(RunForever.props(mat), "runForever");

        Thread.sleep(100);
        runWithMyself.tell("hello", ActorRef.noSender());

        Thread.sleep(1000);
    }

}
