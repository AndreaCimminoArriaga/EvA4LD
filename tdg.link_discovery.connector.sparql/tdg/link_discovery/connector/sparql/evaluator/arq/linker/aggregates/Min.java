package tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates;

import java.util.List;



public class Min extends AbstractARQAggregate{

	public Min() {
		super("Min");
	}
	
	@Override
	public Double applyAggregation(List<Double> values) {
		Double min = 99999999.0;
		for(Double value:values){
			if(value<=min)
				min = value;
		}
		return min;
	}
	
	

}
