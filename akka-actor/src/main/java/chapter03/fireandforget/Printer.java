package chapter03.fireandforget;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Printer {
    public static class PrintMe {
        public final String message;

        public PrintMe(String message) {
            this.message = message;
        }
    }

    public static Behavior<PrintMe> create() {
        return Behaviors.setup(
                context ->
                        Behaviors.receive(PrintMe.class)
                                .onMessage(
                                        PrintMe.class,
                                        printMe -> {
                                            context.getLog().info(printMe.message);
                                            return Behaviors.same();
                                        })
                                .build());
    }
}
