package chapter03.InteractionPatterns.SendFutureResultToSelf;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.concurrent.CompletionStage;

public class CustomerRepository extends AbstractBehavior<CustomerRepository.Command> {

    private static final int MAX_OPERATIONS_IN_PROGRESS = 10;

    interface Command {}

    public static class Update implements Command {
        public final Customer customer;
        public final ActorRef<OperationResult> replyTo;

        public Update(Customer customer, ActorRef<OperationResult> replyTo) {
            this.customer = customer;
            this.replyTo = replyTo;
        }
    }

    interface OperationResult {}

    public static class UpdateSuccess implements OperationResult {
        public final String id;

        public UpdateSuccess(String id) {
            this.id = id;
        }
    }

    public static class UpdateFailure implements OperationResult {
        public final String id;
        public final String reason;

        public UpdateFailure(String id, String reason) {
            this.id = id;
            this.reason = reason;
        }
    }

    private static class WrappedUpdateResult implements Command {
        public final OperationResult result;
        public final ActorRef<OperationResult> replyTo;

        private WrappedUpdateResult(OperationResult result, ActorRef<OperationResult> replyTo) {
            this.result = result;
            this.replyTo = replyTo;
        }
    }

    public static Behavior<Command> create(CustomerDataAccess dataAccess) {
        return Behaviors.setup(context -> new CustomerRepository(context, dataAccess));
    }

    private final CustomerDataAccess dataAccess;
    private int operationsInProgress = 0;

    private CustomerRepository(ActorContext<Command> context, CustomerDataAccess dataAccess) {
        super(context);
        this.dataAccess = dataAccess;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Update.class, this::onUpdate)
                .onMessage(WrappedUpdateResult.class, this::onUpdateResult)
                .build();
    }

    private Behavior<Command> onUpdate(Update command) {
        if (operationsInProgress == MAX_OPERATIONS_IN_PROGRESS) {
            command.replyTo.tell(
                    new UpdateFailure(
                            command.customer.id,
                            "Max " + MAX_OPERATIONS_IN_PROGRESS + " concurrent operations supported"));
        } else {
            // increase operationsInProgress counter
            operationsInProgress++;
            CompletionStage<Done> futureResult = dataAccess.update(command.customer);
            getContext()
                    .pipeToSelf(
                            futureResult,
                            (ok, exc) -> {
                                if (exc == null){
                                    return new WrappedUpdateResult(
                                            new UpdateSuccess(command.customer.id), command.replyTo);
                                }
                                else {
                                    return new WrappedUpdateResult(
                                            new UpdateFailure(command.customer.id, exc.getMessage()),
                                            command.replyTo);
                                }
                            });
        }
        return this;
    }

    private Behavior<Command> onUpdateResult(WrappedUpdateResult wrapped) {
        // decrease operationsInProgress counter
        operationsInProgress--;
        // send result to original requestor
        wrapped.replyTo.tell(wrapped.result);
        return this;
    }
}
