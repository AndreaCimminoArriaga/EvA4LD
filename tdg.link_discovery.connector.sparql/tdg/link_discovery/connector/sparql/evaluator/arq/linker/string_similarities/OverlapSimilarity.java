package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;


public class OverlapSimilarity extends AbstractARQStringSimilarity{

	private StringMetric metric;
	
	public OverlapSimilarity() {
		super("OverlapSimilarity");
		metric = StringMetrics.overlapCoefficient();
	}

	@Override
	public Double compareStrings(String element1, String element2) {	
		return (double) metric.compare(element1, element2);
	}


	
	
}
