package io.kang.akka.stream.chapter01.StreamsQuickstartGuide.MaterializedValues;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.kang.akka.stream.chapter01.StreamsQuickstartGuide.ReactiveTweets.Author;
import io.kang.akka.stream.chapter01.StreamsQuickstartGuide.ReactiveTweets.Tweet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class MainApp {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("reactive-tweets");
        List<Tweet> tweetList = new ArrayList<>();
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka !"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));
        tweetList.add(new Tweet(new Author("rolandkuhn"), System.currentTimeMillis(), "#akka rocks!"));

        Source<Tweet, NotUsed> tweets = Source.from(tweetList);

        final Sink<Integer, CompletionStage<Integer>> sumSink =
                Sink.<Integer, Integer>fold(0, (acc, elem) -> acc + elem);

        final RunnableGraph<CompletionStage<Integer>> counter =
                tweets.map(t -> 1).toMat(sumSink, Keep.right());

        final CompletionStage<Integer> sum = counter.run(system);

        sum.thenAcceptAsync(
                c -> System.out.println("Total tweets processed: " + c), system.dispatcher());
    }
}
