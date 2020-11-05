package chapter03.InteractionPatterns.GeneralPurposeResponseAggregator;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

import java.math.BigDecimal;

public class Hotel1 {
    public static class RequestQuote {
        public final ActorRef<Quote> replyTo;

        public RequestQuote(ActorRef<Quote> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class Quote {
        public final String hotel;
        public final BigDecimal price;

        public Quote(String hotel, BigDecimal price) {
            this.hotel = hotel;
            this.price = price;
        }
        @Override
        public String toString() {
            return String.format("[%s]:[%s]", hotel, price);
        }
    }

    public static Behavior<RequestQuote> create() {
        return Behaviors.receiveMessage(Hotel1::onRequestQuote);
    }

    private static Behavior<RequestQuote> onRequestQuote(RequestQuote message) {
        message.replyTo.tell(new Quote("AA", new BigDecimal(399.0)));
        return Behaviors.same();
    }

}