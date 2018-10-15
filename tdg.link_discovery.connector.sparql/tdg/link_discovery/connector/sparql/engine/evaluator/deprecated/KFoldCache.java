package tdg.link_discovery.connector.sparql.engine.evaluator.deprecated;

import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import tdg.link_discovery.middleware.objects.Tuple;

public class KFoldCache {

	
	public SparqlCache sourceCache, targetCache; // id as key; SparqlCacheInstace as value
	public Set<Tuple<String,String>> positiveReferenceLinks, negativeReferenceLinks;
	public Boolean isTrainingFold;
	
	
	public KFoldCache() {
		positiveReferenceLinks = Sets.newConcurrentHashSet();
		negativeReferenceLinks = Sets.newConcurrentHashSet();
		sourceCache = new SparqlCache();
		targetCache = new SparqlCache();
	}
	
	public Boolean getIsTrainingFold() {
		return isTrainingFold;
	}

	public void setIsTrainingFold(Boolean isTrainingFold) {
		this.isTrainingFold = isTrainingFold;
	}

	public SparqlCache getSourceCache() {
		return sourceCache;
	}

	public void setSourceCache(SparqlCache sourceCache) {
		this.sourceCache = sourceCache;
	}

	public SparqlCache getTargetCache() {
		return targetCache;
	}

	public void setTargetCache(SparqlCache targetCache) {
		this.targetCache = targetCache;
	}

	public Set<Tuple<String, String>> getPositiveReferenceLinks() {
		return positiveReferenceLinks;
	}

	public void setPositiveReferenceLinks(Set<Tuple<String, String>> positiveReferenceLinks) {
		this.positiveReferenceLinks = positiveReferenceLinks;
	}

	public Set<Tuple<String, String>> getNegativeReferenceLinks() {
		return negativeReferenceLinks;
	}

	public void setNegativeReferenceLinks(Set<Tuple<String, String>> negativeReferenceLinks) {
		this.negativeReferenceLinks = negativeReferenceLinks;
	}
	
	
	
}
