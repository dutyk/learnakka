package chapter08.FiniteStateMachines;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Buncher {
    public interface Event {
    }

    public static class SetTarget implements Event {
        public final ActorRef<Buncher.Batch> ref;

        public SetTarget(ActorRef<Buncher.Batch> ref) {
            this.ref = ref;
        }
    }
    private enum Timeout implements Event {
        INSTANCE
    }

    public enum Flush implements Event {
        INSTANCE
    }

    public static final class Queue implements Event {
        public final Object obj;

        public Queue(Object obj) {
            this.obj = obj;
        }

        @Override
        public String toString() {
            return String.valueOf(obj);
        }
    }
    // #simple-events

    // #storing-state
    interface Data {}

    public static final class Todo implements Data {
        public final ActorRef<Batch> target;
        public final List<Object> queue;

        public Todo(ActorRef<Batch> target, List<Object> queue) {
            this.target = target;
            this.queue = queue;
        }

        // #storing-state

        @Override
        public String toString() {
            return "Todo{" + "target=" + target + ", queue=" + queue + '}';
        }

        public Todo addElement(Object element) {
            List<Object> nQueue = new LinkedList<>(queue);
            nQueue.add(element);
            return new Todo(this.target, nQueue);
        }

        public Todo copy(List<Object> queue) {
            return new Todo(this.target, queue);
        }

        public Todo copy(ActorRef<Batch> target) {
            return new Todo(target, this.queue);
        }
        // #storing-state
    }

    public static final class Batch {
        public final List<Object> list;

        public Batch(List<Object> list) {
            this.list = list;
        }

        // #storing-state

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Batch batch = (Batch) o;

            return list.equals(batch.list);
        }

        @Override
        public int hashCode() {
            return list.hashCode();
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("Batch{list=");
            list.stream()
                    .forEachOrdered(
                            e -> {
                                builder.append(e);
                                builder.append(",");
                            });
            int len = builder.length();
            builder.replace(len, len, "}");
            return builder.toString();
        }
        // #storing-state
    }
    // #storing-state

    // #simple-state
    // FSM states represented as behaviors

    // initial state
    public static Behavior<Event> create() {
        return uninitialized();
    }

    static Behavior<Event> uninitialized() {
        return Behaviors.receive(Event.class)
                .onMessage(
                        SetTarget.class, message -> idle(new Todo(message.ref, Collections.emptyList())))
                .build();
    }

    static Behavior<Event> idle(Todo data) {
        return Behaviors.receive(Event.class)
                .onMessage(Queue.class, message -> active(data.addElement(message)))
                .build();
    }

    static Behavior<Event> active(Todo data) {
        return Behaviors.withTimers(
                timers -> {
                    // State timeouts done with withTimers
                    timers.startSingleTimer("Timeout", Timeout.INSTANCE, Duration.ofSeconds(2));
                    return Behaviors.receive(Event.class)
                            .onMessage(Queue.class, message -> active(data.addElement(message)))
                            .onMessage(Flush.class, message -> activeOnFlushOrTimeout(data))
                            .onMessage(Timeout.class, message -> activeOnFlushOrTimeout(data))
                            .build();
                });
    }

    static Behavior<Event> activeOnFlushOrTimeout(Todo data) {
        data.target.tell(new Batch(data.queue));
        return idle(data.copy(new ArrayList<>()));
    }
}
