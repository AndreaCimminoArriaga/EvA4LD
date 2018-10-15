package tdg.link_discovery.connector.sparql.engine.translator;


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.translator.ITranslator;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.Tuple;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class SparqlTranslator implements ITranslator{

	private Map<String,String> sourceStatements;
	private Map<String,String> targetStatements;
	private Multimap<String,String> relatedVariables;
	
	private String sourceAttrRegexPattern, targetAttrRegexPattern;

	private void initStatements(){
		sourceStatements = Maps.newHashMap();
		targetStatements = Maps.newHashMap();
		relatedVariables = ArrayListMultimap.create();
		
		sourceAttrRegexPattern = getAttrPatter(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		targetAttrRegexPattern =  getAttrPatter(FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
	}
	
	private String getAttrPatter(String token){
		StringBuilder pattern = new StringBuilder();
		pattern.append(token).append("[^").append(token).append(",\\)]*").append(token);
		return pattern.toString();
	}
	
	
	public Tuple<String,String> translate(String linkSpecification , IEnvironment environment) {
		initStatements();
		getAttributes(linkSpecification);
	
		StringBuffer querySource = SPARQLFactory.obtainQuery(sourceStatements, true);		
		StringBuffer queryTarget = SPARQLFactory.obtainQuery(targetStatements, false);
		StringBuffer bindingAndFilter = getFilter(linkSpecification);
		queryTarget.append(bindingAndFilter);

		String sourceQueryCleaned = cleanQuery(querySource.toString(), FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		String targetQueryCleaned = cleanQuery(queryTarget.toString(), FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
		// Attach source & target restrictions
	
		String sourceQueryFinal = addRestrictionsToQuery(sourceQueryCleaned, environment.getSourceRestrictions());
		String targetQueryFinal = addRestrictionsToQuery(targetQueryCleaned, environment.getTargetRestrictions());
		
		Tuple<String,String> queries = new Tuple<String,String>(sourceQueryFinal, targetQueryFinal);
		return queries;
	}
	
	@Override
	public Tuple<String,String> translate(ISpecification<?> specification, IEnvironment environment) {
		String linkSpecification  = specification.toString();
		initStatements();
		getAttributes(linkSpecification);
		StringBuffer querySource = SPARQLFactory.obtainQuery(sourceStatements, true);		
		StringBuffer queryTarget = SPARQLFactory.obtainQuery(targetStatements, false);
		StringBuffer bindingAndFilter = getFilter(linkSpecification);
		queryTarget.append(bindingAndFilter);

		String sourceQueryCleaned = cleanQuery(querySource.toString(), FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		String targetQueryCleaned = cleanQuery(queryTarget.toString(), FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
		// Attach source & target restrictions
	
		String sourceQueryFinal = addRestrictionsToQuery(sourceQueryCleaned, environment.getSourceRestrictions());
		String targetQueryFinal = addRestrictionsToQuery(targetQueryCleaned, environment.getTargetRestrictions());
		
		Tuple<String,String> queries = new Tuple<String,String>(sourceQueryFinal, targetQueryFinal);
		return queries;
	}
	
	private String addRestrictionsToQuery(String query, List<String> restrictions){
		String newQuery = query;
	
		if(!restrictions.isEmpty()){
			StringBuilder restrictionStatements = new StringBuilder();
			String mainVar = SPARQLFactory.getMainVariable(query)[0];
			restrictionStatements.append("{\n");
			for(String restriction:restrictions)
				restrictionStatements.append("\t").append(mainVar).append(" rdf:type ").append(SPARQLFactory.fixIRIS(restriction)).append(" .\n");
			newQuery = query.replaceFirst("\\{", restrictionStatements.toString());
		}
		return newQuery;
	}
	
	
	/*private String addRestrictionsToQuery(String query, List<String> restrictions){
		String newQuery = query;
	
		if(!restrictions.isEmpty()){
			StringBuilder restrictionStatements = new StringBuilder();
			String mainVar = SPARQLFactory.getMainVariable(query)[0];
			
			for(String restriction:restrictions)
				restrictionStatements.append("\t").append(mainVar).append(" rdf:type ").append(SPARQLFactory.fixIRIS(restriction)).append(" .\n");
			if(newQuery.contains("#Bind and Filter")) {
				// target query
				restrictionStatements.append("\t#Bind and Filter");
				newQuery = query.replace("\t#Bind and Filter", restrictionStatements.toString());
			}else {
				//source query
				restrictionStatements.append("\n  }");
				newQuery = query.replace("}", restrictionStatements.toString());
			}
			
			
		}
		return newQuery;
	}
	*/
	
	
	@Override
	public Tuple<String,String> translate(ISpecification<?> specification) {
		String linkSpecification  = specification.toString();
		
		initStatements();
		getAttributes(linkSpecification);
		StringBuffer querySource = SPARQLFactory.obtainQuery(sourceStatements, true);		
		StringBuffer queryTarget = SPARQLFactory.obtainQuery(targetStatements, false);
		StringBuffer bindingAndFilter = getFilter(linkSpecification);
		queryTarget.append(bindingAndFilter);

		String sourceQueryCleaned = cleanQuery(querySource.toString(), FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		String targetQueryCleaned = cleanQuery(queryTarget.toString(), FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
		
		Tuple<String,String> queries = new Tuple<String,String>(sourceQueryCleaned, targetQueryCleaned);
		return queries;
	}
	
	private String cleanQuery(String query, String token){
		return query.replace("\u00A0","").replaceAll("\\.\\s+\\.", "\\.");
	}
	
	private void getAttributes(String filter){
		
		// Retrieve the attributes from the filter in the order they appear. Since the string comparison use pairs of attributes each list below
		// has the equivalent attributes from each dataset in the same index.
		List<String> sourceAttributesSorted = retrieveAttributesFollowingPattern(filter, sourceAttrRegexPattern, FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		List<String> targetAttributesSorted = retrieveAttributesFollowingPattern(filter, targetAttrRegexPattern, FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
	
		// Combine the attributes from the datasets
		List<Tuple<String,String>> attributesRetrieved = Lists.newArrayList();
		for(int index=0; index <sourceAttributesSorted.size(); index++){
			String sourceAttr = sourceAttributesSorted.get(index);
			String targetAttr = targetAttributesSorted.get(index);
			Tuple<String,String> pair = new Tuple<String,String>(sourceAttr, targetAttr);
			attributesRetrieved.add(pair);
		}
		// Finally, embed previous attributes into query
		embedAttributesIntoQuery(attributesRetrieved);
	}
	
	// Given a pattern this method matches all the attributes that follow such pattern
	private List<String> retrieveAttributesFollowingPattern(String filter, String regex, String tokenToRemove){
		List<String> attributes = Lists.newArrayList();
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(filter);
		int start = 0;
		
		while (matcher.find(start)) {
			String attribute = matcher.group().replace(tokenToRemove,"");
			attributes.add(attribute);
			start = matcher.start() + 1;
		}
		
		return attributes;
	}
	
	private void embedAttributesIntoQuery(List<Tuple<String,String>> attributePairs) {
		
		for(Tuple<String, String> attributePair:attributePairs){
			String sourceAttr = attributePair.getFirstElement().trim();
			String targetAttr = attributePair.getSecondElement().trim();
			String sourceVar = SPARQLFactory.generateFreshVar();
			String targetVar = SPARQLFactory.generateFreshVar();
			// Add attributes only once
			if(!this.sourceStatements.containsValue(sourceAttr)){
				this.sourceStatements.put(sourceVar,sourceAttr);
			}else{
				sourceVar = getKey(sourceAttr, this.sourceStatements);
			}
			if(!this.targetStatements.containsValue(targetAttr)){
				this.targetStatements.put(targetVar,targetAttr);
			}else{
				targetVar = getKey(targetAttr, this.targetStatements);
			}
			if(!this.relatedVariables.containsEntry(sourceVar,targetVar)){
				this.relatedVariables.put(sourceVar, targetVar);
			}
		}
	}


	private String getKey(String object, Map<String,String> map){
		String keyFound = "";
		for(String key:map.keySet()){
			if(object.equals(map.get(key))){
				keyFound = key;
				break;
			}
		}
		return keyFound;
	}
	
	private StringBuffer getFilter(String filterStr) {
		StringBuffer binding = new StringBuffer();
		binding.append("\tBIND ( ");
		String newFilter = filterStr;
		for(Entry<String, String> relatedVars:this.relatedVariables.entries()){
			String sourceAttr = this.sourceStatements.get(relatedVars.getKey()).replaceAll(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER, "");
			String targetAttr = this.targetStatements.get(relatedVars.getValue()).replaceAll(FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER, "");
			StringBuilder regex = new StringBuilder();
			regex.append("[").append(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
			regex.append(FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER).append("]+");
			
			newFilter = newFilter.replaceAll(regex.toString(),"")
								.replaceAll("\\("+sourceAttr, "("+relatedVars.getKey())
								.replaceAll(targetAttr, relatedVars.getValue())
								.replace(" ","");
			
		}
		binding.append(newFilter);
		String filterVar = SPARQLFactory.generateFreshVar();
		binding.append(" AS ").append(filterVar).append(" ) .\n\tFILTER ( ").append(filterVar).append("> 0 ) .\n}");
		
		return binding;
	}
	
	


	
}
