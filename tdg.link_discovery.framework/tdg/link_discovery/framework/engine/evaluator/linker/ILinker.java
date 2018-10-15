package tdg.link_discovery.framework.engine.evaluator.linker;

import java.util.Set;

import org.apache.jena.rdf.model.Model;

import tdg.link_discovery.middleware.objects.Tuple;

public interface ILinker {

	public abstract Set<Tuple<String, String>> getInstancesLinked();

	public abstract void linkDatasets(Tuple<String, String> queries,
			String outputFile);

	public abstract void linkInstances(Tuple<String, String> queries);

	/*
	 * Getters & Setters
	 */
	public abstract void setDatasetSource(String datasetSource);

	public abstract void setDatasetTarget(String datasetTarget);
	
	public abstract void setDatasetSource(Model datasetSource);

	public abstract void setDatasetTarget(Model datasetTarget);

	public abstract void setInstances(Set<Tuple<String, String>> instances);

}