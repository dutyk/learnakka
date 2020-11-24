package io.kang.akka.stream.chapter01.StreamsQuickstartGuide.ReactiveTweets;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ClosedShape;
import akka.stream.FlowShape;
import akka.stream.SinkShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class MainBroadCast {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("reactive-tweets");
        List<Tweet> tweetList = new ArrayList<>();
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka !"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));

        Source<Tweet, NotUsed> tweets = Source.from(tweetList);

        final Sink<Author, CompletionStage<Done>> writeAuthors = Sink.foreach(a -> System.out.println(a));
        final Sink<String, CompletionStage<Done>> writeHashtags = Sink.foreach(a -> System.out.println(a));

        RunnableGraph.fromGraph(
                GraphDSL.create(
                        b -> {
                            final UniformFanOutShape<Tweet, Tweet> bcast = b.add(Broadcast.create(2));
                            final FlowShape<Tweet, Author> toAuthor =
                                    b.add(Flow.of(Tweet.class).map(t -> t.author));
                            final FlowShape<Tweet, String> toTags =
                                    b.add(
                                            Flow.of(Tweet.class)
                                                    .mapConcat(t -> new ArrayList<String>(t.hashtags())));
                            final SinkShape<Author> authors = b.add(writeAuthors);
                            final SinkShape<String> hashtags = b.add(writeHashtags);

                            b.from(b.add(tweets)).viaFanOut(bcast).via(toAuthor).to(authors);
                            b.from(bcast).via(toTags).to(hashtags);
                            return ClosedShape.getInstance();
                        }))
                .run(system);
    }
}
