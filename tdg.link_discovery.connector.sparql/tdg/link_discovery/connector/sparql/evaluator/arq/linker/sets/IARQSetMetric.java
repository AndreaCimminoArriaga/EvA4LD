package tdg.link_discovery.connector.sparql.evaluator.arq.linker.sets;

import tdg.link_discovery.framework.learner.functions.SetMetricFunction;

public interface IARQSetMetric extends SetMetricFunction{

	public Double applySetMetric(Integer setsIntersection, Integer sourceSetSize, Integer tagetSetSize);
}
