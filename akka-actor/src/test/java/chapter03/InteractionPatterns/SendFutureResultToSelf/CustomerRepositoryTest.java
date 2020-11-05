package chapter03.InteractionPatterns.SendFutureResultToSelf;


import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import chapter03.InteractionPatterns.SendFutureResultToSelf.CustomerRepository.*;
import static junit.framework.TestCase.assertEquals;

public class CustomerRepositoryTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testCustomerRepository() throws InterruptedException {
        CustomerDataAccess customerDataAccess = new CustomerDataAccessImpl();
        ActorRef<Command> customerRepository = testKit.spawn(CustomerRepository.create(customerDataAccess));
        TestProbe<OperationResult> replyTo = testKit.createTestProbe(OperationResult.class);


        Customer customer = new Customer("1", 1, "kang", "sh");

        customerRepository.tell(new CustomerRepository.Update(customer, replyTo.getRef()));


        UpdateSuccess response = (UpdateSuccess) replyTo.receiveMessage();
        assertEquals(response.id, "1");
    }

}
