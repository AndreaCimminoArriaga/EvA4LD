package tdg.link_discovery.connector.h2.evaluator;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.ext.com.google.common.collect.Sets;
import tdg.link_discovery.connector.h2.linker.H2Linker;
import tdg.link_discovery.framework.algorithm.sample.ISampleReader;
import tdg.link_discovery.framework.algorithm.sample.Sample;
import tdg.link_discovery.framework.engine.evaluator.AbstractEvaluator;
import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tuple;

public class H2Evaluator extends AbstractEvaluator {

	private  ILinker linker;
	private Set<Tuple<String,String>> positiveSamples, negativeSamples;
	

	public H2Evaluator(IEnvironment environment, ISampleReader<String> sampleReader){
		super(environment,sampleReader);
		// Read examples using ISampleReader
		@SuppressWarnings("unchecked")
		Collection<Sample<String>> examples = this.sampleReader.readSamplesFromFile(environment.getExamplesFile());
		positiveSamples = examples.stream().filter(ex-> ex.getIsPositive()).map(ex-> new Tuple<String,String>(ex.getElement1(), ex.getElement2())).collect(Collectors.toSet());
		negativeSamples = examples.stream().filter(ex-> !ex.getIsPositive()).map(ex-> new Tuple<String,String>(ex.getElement1(), ex.getElement2())).collect(Collectors.toSet());
		Set<Tuple<String, String>> instances = Sets.union(positiveSamples, negativeSamples);
		// Initialize linker
		this.linker = new H2Linker(FrameworkConfiguration.SOURCE_H2_DATABASE,FrameworkConfiguration.TARGET_H2_DATABASE);
		this.linker.setDatasetSource(this.environment.getSourceDatasetFile());
		this.linker.setDatasetTarget(this.environment.getTargetDatasetFile());
		this.linker.setInstances(instances);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public ConfusionMatrix evaluate(Object object) {
		Tuple<String,String> queries = (Tuple<String,String>) object;
		linker.linkInstances(queries);
		ConfusionMatrix matrix = getMetrics(linker.getInstancesLinked(), positiveSamples, negativeSamples);
		return matrix;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void apply(Object object) {
		Tuple<String,String> queries = (Tuple<String,String>) object;
		linker.linkDatasets(queries, environment.getLinksOutput());
		
	}



	
}
