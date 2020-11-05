package chapter03.InteractionPatterns.IgnoringReplies;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class CookieFabric {
    // #request-response-protocol
    public static class Request {
        public final String query;
        public final ActorRef<Response> replyTo;

        public Request(String query, ActorRef<Response> replyTo) {
            this.query = query;
            this.replyTo = replyTo;
        }
    }

    public static class Response {
        public final String result;

        public Response(String result) {
            this.result = result;
        }
    }
    // #request-response-protocol

    // #request-response-respond
    // actor behavior
    public static Behavior<Request> create() {
        return Behaviors.receive(Request.class)
                .onMessage(Request.class, CookieFabric::onRequest)
                .build();
    }

    private static Behavior<Request> onRequest(Request request) {
        // ... process request ...
        request.replyTo.tell(new Response("Here are the cookies for " + request.query));
        return Behaviors.same();
    }
    // #request-response-respond
}