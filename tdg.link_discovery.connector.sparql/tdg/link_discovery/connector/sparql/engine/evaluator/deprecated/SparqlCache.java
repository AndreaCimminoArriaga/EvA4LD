package tdg.link_discovery.connector.sparql.engine.evaluator.deprecated;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.SparqlCacheInstance;

public class SparqlCache {

	protected Map<String, SparqlCacheInstance> instances;
	
	
	public SparqlCache() {
		instances = Maps.newConcurrentMap();
	}

	public SparqlCacheInstance getInstance(String identifier) {
		SparqlCacheInstance instance = null;
		if(instances.containsKey(identifier))
			instance = instances.get(identifier);
		return instance;
	}
	
	public void addInstance(SparqlCacheInstance instance) {
		instances.put(instance.getId(), instance);
	}
	
	public void addInstances(Collection<SparqlCacheInstance> instances) {
		instances.forEach(instance -> addInstance(instance));
	}
}
