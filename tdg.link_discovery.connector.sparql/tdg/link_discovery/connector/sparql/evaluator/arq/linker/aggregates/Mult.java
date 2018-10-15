package tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates;

import java.util.List;



public class Mult extends AbstractARQAggregate{

	public Mult() {
		super("Mult");
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
	
	public Double applyAggregation(Double val1, Double val2) {
		return val1 * val2;
	}

	

}
