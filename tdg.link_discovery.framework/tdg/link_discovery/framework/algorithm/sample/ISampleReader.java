package tdg.link_discovery.framework.algorithm.sample;

import java.util.Collection;

public interface ISampleReader<T> {

	public Collection<Sample<T>> readSamplesFromFile(String file);

}
