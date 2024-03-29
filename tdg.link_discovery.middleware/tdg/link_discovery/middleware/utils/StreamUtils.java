package tdg.link_discovery.middleware.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {
	

	    public static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
	        return asStream(sourceIterator, false);
	    }

	    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
	        Iterable<T> iterable = () -> sourceIterator;
	        return StreamSupport.stream(iterable.spliterator(), parallel);
	    }
	
	    public static <T> Stream<T> asStream(T[] sourceArray) {
	        return Arrays.stream(sourceArray);
	    }
}
