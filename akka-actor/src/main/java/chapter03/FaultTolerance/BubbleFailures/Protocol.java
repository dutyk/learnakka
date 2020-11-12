package chapter03.FaultTolerance.BubbleFailures;

import akka.actor.typed.ActorRef;

public class Protocol {
    public interface Command {}

    public static class Fail implements Command {
        public final String text;

        public Fail(String text) {
            this.text = text;
        }
    }

    public static class Hello implements Command {
        public final String text;
        public final ActorRef<String> replyTo;

        public Hello(String text, ActorRef<String> replyTo) {
            this.text = text;
            this.replyTo = replyTo;
        }
    }
}
