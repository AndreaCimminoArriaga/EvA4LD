package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import java.util.List;

import tdg.link_discovery.framework.learner.functions.StringMetricFunction;


public interface IARQStringSimilarity extends StringMetricFunction{
	@Override
	public Double compareStrings(String element1, String element2);
	
	public Double compareStrings(List<String> element1, List<String> element2);
	public Double similarity(String value1, String value2, Double threshold);
}
