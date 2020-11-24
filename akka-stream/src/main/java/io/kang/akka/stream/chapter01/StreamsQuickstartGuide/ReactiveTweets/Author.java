package io.kang.akka.stream.chapter01.StreamsQuickstartGuide.ReactiveTweets;

public class Author {
    public final String handle;

    public Author(String handle) {
        this.handle = handle;
    }

    @Override
    public String toString() {
        return String.format("author:%s", handle);
    }
}
