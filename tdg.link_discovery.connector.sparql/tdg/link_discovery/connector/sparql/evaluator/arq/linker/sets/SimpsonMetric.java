package tdg.link_discovery.connector.sparql.evaluator.arq.linker.sets;

public class SimpsonMetric extends AbstractARQSetMetric{

	public SimpsonMetric() {
		super("SimpsonMetric");
	}

	@Override
	public Double applySetMetric(Integer setsIntersection, Integer sourceSetSize, Integer tagetSetSize) {
		return (setsIntersection*1.0)/Math.min(sourceSetSize, tagetSetSize);
	}

	
	

}
