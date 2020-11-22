package chapter13.Coexistence;

public class ClassicApp {
    public static void main(String[] args) {
        akka.actor.ActorSystem as = akka.actor.ActorSystem.create();
        akka.actor.ActorRef classic = as.actorOf(Classic.props());
    }
}
