package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import info.debatty.java.stringsimilarity.MetricLCS;

public class SubstringSimilarity extends AbstractARQStringSimilarity{

	private MetricLCS lcs;
	
	public SubstringSimilarity() {
		super("SubstringSimilarity");
		lcs =  new MetricLCS();
	}


	@Override
	public Double compareStrings(String element1, String element2) {
		return 1.0 - lcs.distance(element1, element2);
	}

	
	
}
