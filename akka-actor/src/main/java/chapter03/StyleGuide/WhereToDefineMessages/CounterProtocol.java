package chapter03.StyleGuide.WhereToDefineMessages;

import akka.actor.typed.ActorRef;

interface CounterProtocol {
    interface Command {}

    public static class Increment implements Command {
        public final int delta;
        private final ActorRef<OperationResult> replyTo;

        public Increment(int delta, ActorRef<OperationResult> replyTo) {
            this.delta = delta;
            this.replyTo = replyTo;
        }
    }

    public static class Decrement implements Command {
        public final int delta;
        private final ActorRef<OperationResult> replyTo;

        public Decrement(int delta, ActorRef<OperationResult> replyTo) {
            this.delta = delta;
            this.replyTo = replyTo;
        }
    }

    interface OperationResult {}

    enum Confirmed implements OperationResult {
        INSTANCE
    }

    public static class Rejected implements OperationResult {
        public final String reason;

        public Rejected(String reason) {
            this.reason = reason;
        }
    }
}
