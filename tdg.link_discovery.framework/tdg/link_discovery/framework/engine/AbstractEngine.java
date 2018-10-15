package tdg.link_discovery.framework.engine;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.evaluator.IEvaluator;
import tdg.link_discovery.framework.engine.translator.ITranslator;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;



public abstract class AbstractEngine implements IEngine{

	protected ITranslator translator;
	protected IEvaluator evaluator;
	protected String name;
	
	public AbstractEngine(){
		// Initi the attribute with specific translator and evaluator depending on the dataset format
		this.name = "unnamed engine";
	}
	
	public AbstractEngine(String name){
		// Initi the attribute with specific translator and evaluator depending on the dataset format
		this.name = name;
	}
	
	
	@Override
	public void setTranslator(ITranslator translator) {
		this.translator = translator;
	}
	
	@Override
	public void setEvaluator(IEvaluator evaluator) {
		this.evaluator = evaluator;
	}
	
	
	@Override
	public <T extends Object> ConfusionMatrix evaluate(ISpecification<T> specification) {
		Object lowLevelRepresentation = this.translator.translate(specification, this.evaluator.getEnvironment());
		ConfusionMatrix confusionmatrix = this.evaluator.evaluate(lowLevelRepresentation);
		SPARQLFactory.usedVariables.clear(); // cleans the cache of used variables in the generated queries to free memory
		return confusionmatrix;
	}

	
	@Override
	public <T extends Object> void linkData(ISpecification<T> specification) {
		Object lowLevelRepresentation = this.translator.translate(specification, this.evaluator.getEnvironment());
		this.evaluator.apply(lowLevelRepresentation);
		SPARQLFactory.usedVariables.clear(); // cleans the cache of used variables in the generated queries to free memory
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	


}
