package chapter03.InteractionPatterns.PerSessionChildActor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.Optional;

public class Home {
    public interface Command {}

    public static class LeaveHome implements Command {
        public final String who;
        public final ActorRef<ReadyToLeaveHome> respondTo;

        public LeaveHome(String who, ActorRef<ReadyToLeaveHome> respondTo) {
            this.who = who;
            this.respondTo = respondTo;
        }
    }

    public static class ReadyToLeaveHome {
        public final String who;
        public final Keys keys;
        public final Wallet wallet;

        public ReadyToLeaveHome(String who, Keys keys, Wallet wallet) {
            this.who = who;
            this.keys = keys;
            this.wallet = wallet;
        }
    }

    private final ActorContext<Command> context;

    private final ActorRef<KeyCabinet.GetKeys> keyCabinet;
    private final ActorRef<Drawer.GetWallet> drawer;

    private Home(ActorContext<Command> context) {
        this.context = context;
        this.keyCabinet = context.spawn(KeyCabinet.create(), "key-cabinet");
        this.drawer = context.spawn(Drawer.create(), "drawer");
    }

    private Behavior<Command> behavior() {
        return Behaviors.receive(Command.class)
                .onMessage(LeaveHome.class, this::onLeaveHome)
                .build();
    }

    private Behavior<Command> onLeaveHome(LeaveHome message) {
        context.spawn(
                PrepareToLeaveHome.create(message.who, message.respondTo, keyCabinet, drawer),
                "leaving" + message.who);
        return Behaviors.same();
    }

    // actor behavior
    public static Behavior<Command> create() {
        return Behaviors.setup(context -> new Home(context).behavior());
    }
}

// per session actor behavior
class PrepareToLeaveHome extends AbstractBehavior<Object> {
    static Behavior<Object> create(
            String whoIsLeaving,
            ActorRef<Home.ReadyToLeaveHome> replyTo,
            ActorRef<KeyCabinet.GetKeys> keyCabinet,
            ActorRef<Drawer.GetWallet> drawer) {
        return Behaviors.setup(
                context -> new PrepareToLeaveHome(context, whoIsLeaving, replyTo, keyCabinet, drawer));
    }

    private final String whoIsLeaving;
    private final ActorRef<Home.ReadyToLeaveHome> replyTo;
    private final ActorRef<KeyCabinet.GetKeys> keyCabinet;
    private final ActorRef<Drawer.GetWallet> drawer;
    private Optional<Wallet> wallet = Optional.empty();
    private Optional<Keys> keys = Optional.empty();

    private PrepareToLeaveHome(
            ActorContext<Object> context,
            String whoIsLeaving,
            ActorRef<Home.ReadyToLeaveHome> replyTo,
            ActorRef<KeyCabinet.GetKeys> keyCabinet,
            ActorRef<Drawer.GetWallet> drawer) {
        super(context);
        this.whoIsLeaving = whoIsLeaving;
        this.replyTo = replyTo;
        this.keyCabinet = keyCabinet;
        this.drawer = drawer;
        this.keyCabinet.tell(new KeyCabinet.GetKeys("yuan", getContext().getSelf()));
        this.drawer.tell(new Drawer.GetWallet("yuan", getContext().getSelf()));
    }

    @Override
    public Receive<Object> createReceive() {
        return newReceiveBuilder()
                .onMessage(Wallet.class, this::onWallet)
                .onMessage(Keys.class, this::onKeys)
                .build();
    }

    private Behavior<Object> onWallet(Wallet wallet) {
        this.wallet = Optional.of(wallet);
        return completeOrContinue();
    }

    private Behavior<Object> onKeys(Keys keys) {
        this.keys = Optional.of(keys);
        return completeOrContinue();
    }

    private Behavior<Object> completeOrContinue() {
        if (wallet.isPresent() && keys.isPresent()) {
            replyTo.tell(new Home.ReadyToLeaveHome(whoIsLeaving, keys.get(), wallet.get()));
            return Behaviors.stopped();
        } else {
            return this;
        }
    }
}
