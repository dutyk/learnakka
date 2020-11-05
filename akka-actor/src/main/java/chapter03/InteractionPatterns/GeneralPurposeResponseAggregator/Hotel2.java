package chapter03.InteractionPatterns.GeneralPurposeResponseAggregator;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

import java.math.BigDecimal;

public class Hotel2 {
    public static class RequestPrice {
        public final ActorRef<Price> replyTo;

        public RequestPrice(ActorRef<Price> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class Price {
        public final String hotel;
        public final BigDecimal price;

        public Price(String hotel, BigDecimal price) {
            this.hotel = hotel;
            this.price = price;
        }

        @Override
        public String toString() {
            return String.format("[%s]:[%s]", hotel, price);
        }
    }

    public static Behavior<RequestPrice> create() {
        return Behaviors.receiveMessage(Hotel2::onRequestPrice);
    }

    private static Behavior<RequestPrice> onRequestPrice(RequestPrice message) {
        message.replyTo.tell(new Price("BB", new BigDecimal(499.0)));
        return Behaviors.same();
    }

}
