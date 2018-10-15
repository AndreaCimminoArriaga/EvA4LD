package tdg.link_discovery.connector.h2.engine.translator;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jena.ext.com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.translator.ITranslator;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.Tuple;

public class H2Translator implements ITranslator{

	private String sourceAttrRegexPattern, targetAttrRegexPattern;
	
	private void initStatements(){
		sourceAttrRegexPattern = getAttrPatter(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		targetAttrRegexPattern =  getAttrPatter(FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
	}
	
	private String getAttrPatter(String token){
		StringBuilder pattern = new StringBuilder();
		pattern.append(token).append("[^").append(token).append(",\\)]*").append(token);
		return pattern.toString();
	}
	
	@Deprecated
	@Override
	public Object translate(ISpecification<?> specification) {
		return null;
	}

	@Override
	public Object translate(ISpecification<?> specification, IEnvironment environment) {
		String linkSpecification  = specification.toString();
		initStatements();
		Tuple<String,String> queries = getSQLQueries(adaptfilter(linkSpecification), fixTableName(environment.getSourceDatasetFile()), fixTableName(environment.getTargetDatasetFile()));
		return queries;
	}
	
	private String adaptfilter(String linkSpecification) {
		linkSpecification = linkSpecification.replace(SPARQLFactory.prefixJenaFunctionsAggregations, "");
		linkSpecification = linkSpecification.replace(SPARQLFactory.prefixJenaFunctionsStrings, "");
		linkSpecification = linkSpecification.replace(SPARQLFactory.prefixJenaFunctionsTransformations, "");
		return linkSpecification;
	}
	
	private Tuple<String,String> getSQLQueries(String filter, String sourceTable, String targetTable){
		// Retrieve the attributes from the filter in the order they appear. Since the string comparison use pairs of attributes each list below
		// has the equivalent attributes from each dataset in the same index.
		Set<String> sourceAttributesSorted = retrieveAttributesFollowingPattern(filter, sourceAttrRegexPattern, FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		Set<String> targetAttributesSorted = retrieveAttributesFollowingPattern(filter, targetAttrRegexPattern, FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
		// Embedded attributes into queries
		String sourceQuery = transformToSourceQuery(sourceAttributesSorted, sourceTable,filter);
		String targetQuery = transformToTargetQuery(targetAttributesSorted, targetTable,filter, sourceAttributesSorted, sourceTable, sourceQuery);
		return new Tuple<String,String>(sourceQuery, targetQuery);
	}
	
	// Given a list of attributes and a table creates a SQL query to retrieve them 
	private String transformToSourceQuery(Set<String> attributes, String tableName, String filter) {
		StringBuffer query = new StringBuffer("SELECT \""+tableName+"\".\"id\" AS \"idSource\", ");
		attributes.stream().forEach(attr -> query.append("\"").append(tableName).append("\".\"").append(attr).append("\", "));
		query.append("FROM \"").append(fixTableName(tableName)).append("\"");
		StringBuffer placeholder = new StringBuffer();
		placeholder.append("\"").append(tableName).append("\".\"");
		
		return query.toString().replace(", FROM", " FROM");
	}
	
	// Given a list of attributes and a table creates a SQL query to retrieve them 
	private String transformToTargetQuery(Set<String> attributesTarget, String tableNameTarget, String filter, Set<String> attributesSource, String tableNameSource, String sourceQuery) {
			StringBuffer query = new StringBuffer(sourceQuery.substring(0, sourceQuery.indexOf("FROM"))); // use the source query select
			// append new target atributes
			query.append(", \""+tableNameTarget+"\".\"id\" AS \"idTarget\", ");
			attributesTarget.stream().forEach(attr -> query.append("\"").append(tableNameTarget).append("\".\"").append(attr).append("\", "));
			// append tables to query
			query.append("FROM \"").append(fixTableName(tableNameSource)).append("\", \"").append(fixTableName(tableNameTarget)).append("\"");
			
			for(String attr:attributesSource) 
				filter = filter.replace(wrapWithString(attr,FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER), wrapAttributeInSQLFormat(tableNameSource, attr));
			
			for(String attr:attributesTarget) 
				filter = filter.replace(wrapWithString(attr,FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER), wrapAttributeInSQLFormat(tableNameTarget, attr));
							
			query.append(" WHERE 0 < ").append(filter).append(" AND ").append("\"").append(tableNameSource).append("\".\"id\"=");
			return query.toString().replace(", FROM", " FROM");
	}
	
	private String wrapAttributeInSQLFormat(String table, String attr) {
		StringBuffer placeholder = new StringBuffer();
		placeholder.append("\"").append(table).append("\".\"");
		placeholder.append(attr).append("\"");
		return placeholder.toString();
	}
	

	private CharSequence wrapWithString(String value, String token) {
		StringBuffer str = new StringBuffer();
		str.append(token).append(value).append(token);
		return str.toString();
	}

	private String fixTableName(String tableName) {
		String newTableName = tableName;
		if(tableName.contains("/"))
			newTableName = tableName.substring(tableName.lastIndexOf("/")+1, tableName.length());
		return newTableName;
	}
	
	// Given a pattern this method matches all the attributes that follow such
	// pattern
	private Set<String> retrieveAttributesFollowingPattern(String filter, String regex, String tokenToRemove) {
		Set<String> attributes = Sets.newHashSet();
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(filter);
		int start = 0;

		while (matcher.find(start)) {
			String attribute = matcher.group().replace(tokenToRemove, "");
			attributes.add(attribute);
			start = matcher.start() + 1;
		}

		return attributes;
	}

}
