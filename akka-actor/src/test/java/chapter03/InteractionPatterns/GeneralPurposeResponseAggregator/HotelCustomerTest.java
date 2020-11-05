package chapter03.InteractionPatterns.GeneralPurposeResponseAggregator;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import chapter03.InteractionPatterns.GeneralPurposeResponseAggregator.HotelCustomer.*;
import chapter03.InteractionPatterns.GeneralPurposeResponseAggregator.Hotel1.*;
import chapter03.InteractionPatterns.GeneralPurposeResponseAggregator.Hotel2.*;

public class HotelCustomerTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testHotel() {
        ActorRef<RequestQuote> hotel1Actor = testKit.spawn(Hotel1.create());
        ActorRef<RequestPrice> hotel2Actor = testKit.spawn(Hotel2.create());

        ActorRef<Command> hotelActor = testKit.spawn(HotelCustomer.create(hotel1Actor, hotel2Actor));
        TestProbe<AggregatedQuotes> aggregatedQuotesTestProbe = testKit.createTestProbe(AggregatedQuotes.class);
    }

}
