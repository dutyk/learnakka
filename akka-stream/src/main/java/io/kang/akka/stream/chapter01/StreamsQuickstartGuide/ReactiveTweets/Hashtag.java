package io.kang.akka.stream.chapter01.StreamsQuickstartGuide.ReactiveTweets;

public class Hashtag {
    public final String name;

    public Hashtag(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return name.equals(((Hashtag)obj).name);
    }
}
