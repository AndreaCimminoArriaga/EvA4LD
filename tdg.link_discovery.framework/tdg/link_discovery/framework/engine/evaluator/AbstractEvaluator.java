package tdg.link_discovery.framework.engine.evaluator;

import java.util.Set;

import tdg.link_discovery.framework.algorithm.sample.ISampleReader;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tuple;



public abstract class AbstractEvaluator implements IEvaluator{

	public IEnvironment environment;
	
	@SuppressWarnings("rawtypes")
	public ISampleReader sampleReader; // The specific type of ISampleReader is setted in the class that extends the Evaluator
	
	public AbstractEvaluator(){
		// Empty
	}
	
	@SuppressWarnings("rawtypes")
	public AbstractEvaluator(IEnvironment environment, ISampleReader sampleReader) {
		super();
		this.environment = environment;
		this.sampleReader = sampleReader;
	}

	public AbstractEvaluator(IEnvironment environment){
		this.environment = environment;	
	}
	
	@Override
	public void setExperimentalEnvironment(IEnvironment environment) {
		this.environment = environment;	
	}

	@Override
	public <T extends Object> void setSampleReader(ISampleReader<T> sampleReader) {
		this.sampleReader = sampleReader;
	}
	

	@Override
	public IEnvironment getEnvironment() {
		return this.environment;
	}
	
	public ConfusionMatrix getMetrics(Set<Tuple<String,String>> instancesLinked, Set<Tuple<String, String>> positive, Set<Tuple<String, String>> negative){
		ConfusionMatrix metrics = new ConfusionMatrix();
		Integer truePositives = 0;
		Integer falsePositives = 0;
		
		for(Tuple<String,String> irisLinked: instancesLinked){
			if(positive.contains(irisLinked))
				truePositives++;
			if(negative.contains(irisLinked))
				falsePositives++;	
		}
		Integer falseNegatives = Math.abs(positive.size()- truePositives);
		Integer trueNegatives = negative.size() - falsePositives;
		metrics.setTruePositives(truePositives);
		metrics.setFalsePositives(falsePositives);
		metrics.setTrueNegatives(trueNegatives);
		metrics.setFalseNegatives(falseNegatives);
		return metrics;
	}
	
}
