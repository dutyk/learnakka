package chapter03.test.AsynchronousTesting;

import akka.actor.typed.ActorRef;

public class Message {
    int i;
    ActorRef<Integer> replyTo;

    Message(int i, ActorRef<Integer> replyTo) {
        this.i = i;
        this.replyTo = replyTo;
    }
}