package io.kang.akka.stream.chapter04.graph;

import java.util.Iterator;

public class Ints implements Iterator<Integer> {
    private int next = 0;

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Integer next() {
        return next++;
    }
}