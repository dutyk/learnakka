package io.kang.akka.iot.part3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.Optional;

public class Device extends AbstractBehavior<Device.Command> {
    public interface Command {}

    public static final class RecordTemperature implements Command {
        public final long requestId;
        final double value;
        final ActorRef<TemperatureRecorded> replyTo;

        public RecordTemperature(long requestId, double value, ActorRef<TemperatureRecorded> replyTo) {
            this.requestId = requestId;
            this.value = value;
            this.replyTo = replyTo;
        }
    }

    public static final class TemperatureRecorded {
        public final long requestId;

        public TemperatureRecorded(long requestId) {
            this.requestId = requestId;
        }
    }

    public static final class ReadTemperature implements Command {
        final long requestId;
        final ActorRef<RespondTemperature> replyTo;

        public ReadTemperature(long requestId, ActorRef<RespondTemperature> replyTo) {
            this.requestId = requestId;
            this.replyTo = replyTo;
        }
    }

    // #respond-declare
    public static final class RespondTemperature {
        public final long requestId;
        public final String deviceId;
        public final Optional<Double> value;

        public RespondTemperature(long requestId, String deviceId, Optional<Double> value) {
            this.requestId = requestId;
            this.deviceId = deviceId;
            this.value = value;
        }
    }

    public static Behavior<Command> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Device(context, groupId, deviceId));
    }

    private final String groupId;
    private final String deviceId;

    private Optional<Double> lastTemperatureReading = Optional.empty();

    private Device(ActorContext<Command> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;

        context.getLog().info("Device actor {}-{} started", groupId, deviceId);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RecordTemperature.class, this::onRecordTemperature)
                .onMessage(ReadTemperature.class, this::onReadTemperature)
                .onMessage(Passivate.class, m -> Behaviors.stopped())
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    public static enum Passivate implements Command {
        INSTANCE
    }

    private Behavior<Command> onRecordTemperature(RecordTemperature r) {
        getContext().getLog().info("Recorded temperature reading {} with {}", r.value, r.requestId);
        lastTemperatureReading = Optional.of(r.value);
        r.replyTo.tell(new TemperatureRecorded(r.requestId));
        return this;
    }

    private Behavior<Command> onReadTemperature(ReadTemperature r) {
        r.replyTo.tell(new RespondTemperature(r.requestId, deviceId, lastTemperatureReading));
        return this;
    }

    private Behavior<Command> onPostStop() {
        getContext().getLog().info("Device actor {}-{} stopped", groupId, deviceId);
        return Behaviors.stopped();
    }
}