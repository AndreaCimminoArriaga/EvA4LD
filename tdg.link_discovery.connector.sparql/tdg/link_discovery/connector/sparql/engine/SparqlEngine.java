package tdg.link_discovery.connector.sparql.engine;

import java.util.Map;

import tdg.link_discovery.connector.sparql.engine.evaluator.SparqlEvaluator;
import tdg.link_discovery.connector.sparql.engine.sample_reader.NTSampleReader;
import tdg.link_discovery.connector.sparql.engine.translator.SparqlTranslator;
import tdg.link_discovery.framework.algorithm.sample.ISampleReader;
import tdg.link_discovery.framework.engine.AbstractEngine;
import tdg.link_discovery.framework.environment.IEnvironment;

public class SparqlEngine extends AbstractEngine{

	public SparqlEngine(){
		super("SparqlEngine");
		this.translator = new SparqlTranslator();
		this.evaluator = null;
	}
	
	public SparqlEngine(IEnvironment environment){
		super("SparqlEngine");
		this.translator = new SparqlTranslator();
		ISampleReader<String> sampleReader = new NTSampleReader();
		this.evaluator = new SparqlEvaluator(environment, sampleReader);
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
