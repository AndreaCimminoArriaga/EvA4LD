package tdg.link_discovery.connector.sparql.engine.evaluator;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.engine.evaluator.linker.Linker;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.sample.ISampleReader;
import tdg.link_discovery.framework.algorithm.sample.Sample;
import tdg.link_discovery.framework.engine.evaluator.AbstractEvaluator;
import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tuple;

public class SparqlEvaluator extends AbstractEvaluator {


	private  ILinker linker;
	private Set<Tuple<String,String>> positiveSamples, negativeSamples;
	
	@SuppressWarnings("unchecked")
	public SparqlEvaluator(IEnvironment environment, ISampleReader<String> sampleReader){
		super(environment,sampleReader);
		// Read examples using ISampleReader
		Collection<Sample<String>> examples = this.sampleReader.readSamplesFromFile(environment.getExamplesFile());
		positiveSamples = examples.stream().filter(ex-> ex.getIsPositive()).map(ex-> new Tuple<String,String>(ex.getElement1(), ex.getElement2())).collect(Collectors.toSet());
		negativeSamples = examples.stream().filter(ex-> !ex.getIsPositive()).map(ex-> new Tuple<String,String>(ex.getElement1(), ex.getElement2())).collect(Collectors.toSet());
		Set<Tuple<String, String>> instances = Sets.union(positiveSamples, negativeSamples);
		// Initialize linker
		this.linker = new Linker();
		this.linker.setDatasetSource(this.environment.getSourceDatasetFile());
		this.linker.setDatasetTarget(this.environment.getTargetDatasetFile());
		this.linker.setInstances(instances);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public ConfusionMatrix evaluate(Object object) {
		Tuple<String,String> queries = (Tuple<String, String>) object;
		linker.linkInstances(queries);
		ConfusionMatrix matrix = getMetrics(linker.getInstancesLinked(), positiveSamples, negativeSamples);
		//System.out.println("Generated "+linker.getInstancesLinked().size()+" links");
		SPARQLFactory.usedVariables.clear(); // cleans the cache of used variables in the generated queries to free memory
		
		return matrix;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void apply(Object object) {
		Tuple<String,String> queries = (Tuple<String, String>) object;
		linker.linkDatasets(queries, environment.getLinksOutput());
		SPARQLFactory.usedVariables.clear(); // cleans the cache of used variables in the generated queries to free memory
	}



	


}
