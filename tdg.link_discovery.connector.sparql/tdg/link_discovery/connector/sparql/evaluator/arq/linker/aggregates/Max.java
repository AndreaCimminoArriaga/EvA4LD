package tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates;

import java.util.List;


public class Max extends AbstractARQAggregate{

	public Max() {
		super("Maximum");
	}
	
	@Override
	public Double applyAggregation(List<Double> values) {
		Double max = 0.0;
		for(Double value:values){
			if(value>=max)
				max = value;
		}
		return max;
	}

	

}
