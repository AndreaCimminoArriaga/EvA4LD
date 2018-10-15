package tdg.link_discovery.middleware.moea.comparators;

import java.util.Comparator;

import org.moeaframework.core.Solution;

public class SolutionComparator implements Comparator<Solution>{

	@Override
	public int compare(Solution o1, Solution o2) {
		Double v1 = o1.getObjective(0);
		Double v2 = o2.getObjective(0);
		return v1.compareTo(v2);
	}

}
