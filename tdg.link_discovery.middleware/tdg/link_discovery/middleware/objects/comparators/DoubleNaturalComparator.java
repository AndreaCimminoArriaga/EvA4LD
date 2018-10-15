package tdg.link_discovery.middleware.objects.comparators;

import java.util.Comparator;

public class DoubleNaturalComparator implements Comparator<Double>{

	@Override
	public int compare(Double o1, Double o2) {
		return o1.compareTo(o2);
	}

}
