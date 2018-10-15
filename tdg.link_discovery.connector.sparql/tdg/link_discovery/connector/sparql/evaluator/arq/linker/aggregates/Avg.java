package tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates;

import java.util.List;

public class Avg extends AbstractARQAggregate{

	public Avg() {
		super("Avg");
	}
	
	@Override
	public Double applyAggregation(List<Double> values) {
		Double sum = 0.0;
		for(Double value:values){
			sum += value;
		}
		Double avg = sum/values.size();
		return avg;
	}
	

}
