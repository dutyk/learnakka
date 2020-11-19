package chapter03.test.AsynchronousTesting;

import akka.actor.typed.Scheduler;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AskPattern;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

public class Producer {
    private Scheduler scheduler;
    private ActorRef<Message> publisher;

    Producer(Scheduler scheduler, ActorRef<Message> publisher) {
        this.scheduler = scheduler;
        this.publisher = publisher;
    }

    public void produce(int messages) {
        IntStream.range(0, messages).forEach(this::publish);
    }

    private CompletionStage<Integer> publish(int i) {
        return AskPattern.ask(
                publisher,
                (ActorRef<Integer> ref) -> new Message(i, ref),
                Duration.ofSeconds(3),
                scheduler);
    }
}
