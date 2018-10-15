package tdg.link_discovery.connector.h2.engine;

import java.util.Map;

import tdg.link_discovery.connector.h2.engine.translator.H2Translator;
import tdg.link_discovery.connector.h2.evaluator.H2Evaluator;
import tdg.link_discovery.connector.sparql.engine.sample_reader.NTSampleReader;
import tdg.link_discovery.framework.algorithm.sample.ISampleReader;
import tdg.link_discovery.framework.engine.AbstractEngine;
import tdg.link_discovery.framework.environment.IEnvironment;

public class H2Engine extends AbstractEngine{

	public H2Engine(){
		super("H2Engine");
		this.translator = new H2Translator();
		this.evaluator = null;
	}
	
	public H2Engine(IEnvironment environment){
		super("H2Engine");
		this.translator =  new H2Translator();
		ISampleReader<String> sampleReader =  new NTSampleReader();
		this.evaluator = new H2Evaluator(environment, sampleReader);
	}
	
	@Override
	public Map<String, Double> getInputDefaultParameters() {
		return null;
	}

	@Override
	public Boolean hasInputParameters() {
		return false;
	}

	@Override
	public void setInputParameters(String parameterName, Object value) {
		// empty
	}
}
