package chapter03.fireandforget;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;

public class PrinterMain {
    public static void main(String[] args) {
        final ActorSystem<Printer.PrintMe> system =
                ActorSystem.create(Printer.create(), "printer-sample-system");

        // note that system is also the ActorRef to the guardian actor
        final ActorRef<Printer.PrintMe> ref = system;

        // these are all fire and forget
        ref.tell(new Printer.PrintMe("message 1"));
        ref.tell(new Printer.PrintMe("message 2"));
    }
}
