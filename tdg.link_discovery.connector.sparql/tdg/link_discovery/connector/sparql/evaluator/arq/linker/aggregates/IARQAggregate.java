package tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates;

import java.util.List;

import tdg.link_discovery.framework.learner.functions.AggregateFunction;

public interface IARQAggregate extends AggregateFunction{
	public Double applyAggregation(List<Double> values);
	
}
