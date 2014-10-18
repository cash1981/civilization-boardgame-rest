package no.asgari.civilization.server.util;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Java8Util {

    public static <T> Stream<T> streamFromIterable(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), false);
    }

    public static <T> Stream<T> parallelStreamFromIterable(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), true);
    }
}
