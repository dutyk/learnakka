package io.kang.akka.stream.chapter06;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Source;

public class Buffers04 {
    static class Job {}

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("Buffers");

        final Source<Job, NotUsed> inboundJobsConnector = Source.empty();
        // #explicit-buffers-backpressure
        // Getting a stream of jobs from an imaginary external system as a Source
        final Source<Job, NotUsed> jobs = inboundJobsConnector;
        jobs.buffer(1000, OverflowStrategy.backpressure());
        // #explicit-buffers-backpressure

        // #explicit-buffers-droptail
        jobs.buffer(1000, OverflowStrategy.dropTail());
        // #explicit-buffers-droptail

        // #explicit-buffers-dropnew
        jobs.buffer(1000, OverflowStrategy.dropNew());
        // #explicit-buffers-dropnew

        // #explicit-buffers-drophead
        jobs.buffer(1000, OverflowStrategy.dropHead());
        // #explicit-buffers-drophead

        // #explicit-buffers-dropbuffer
        jobs.buffer(1000, OverflowStrategy.dropBuffer());
        // #explicit-buffers-dropbuffer

        // #explicit-buffers-fail
        jobs.buffer(1000, OverflowStrategy.fail());
        // #explicit-buffers-fail
    }
}
