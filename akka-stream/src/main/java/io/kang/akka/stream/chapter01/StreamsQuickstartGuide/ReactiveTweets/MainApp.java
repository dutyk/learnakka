package io.kang.akka.stream.chapter01.StreamsQuickstartGuide.ReactiveTweets;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.List;

public class MainApp {
    public static final String AKKA = "#akka";

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("reactive-tweets");
        List<Tweet> tweetList = new ArrayList<>();
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka !"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));

        Source<Tweet, NotUsed> tweets = Source.from(tweetList);

        final Source<String, NotUsed> hashtags =
                tweets.mapConcat(t -> new ArrayList<String>(t.hashtags()));

        final Source<Author, NotUsed> authors =
                tweets.filter(t -> t.hashtags().contains(AKKA)).map(t -> t.author);

        authors.runWith(Sink.foreach(a -> System.out.println(a)), system);

        authors.runForeach(a -> System.out.println(a), system);

        hashtags.runForeach(a -> System.out.println(a), system);

        tweets
                .buffer(10, OverflowStrategy.dropHead())
                .map(t -> slowComputation(t))
                .runWith(Sink.foreach(o -> System.out.println(o)), system);


    }

    private static long slowComputation(Tweet t) {
        try {
            // act as if performing some heavy computation
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        return 42;
    }
}
