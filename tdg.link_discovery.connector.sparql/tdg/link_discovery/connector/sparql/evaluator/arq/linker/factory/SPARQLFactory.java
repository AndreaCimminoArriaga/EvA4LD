package tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory;


import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;


public class SPARQLFactory {
	
	public static final String QUERY_TOKEN_TO_REPLACE = "#@#";
	public static final Integer DECIMAL_PRECISION = 4;
	public static final Integer LINKER_RESULTS_SAVING_CHUNK = 500;
	
	public static final String prefixJenaFunctionsAggregations = "agg:";
	public static final String prefixJenaFunctionsStrings = "str:";
	public static final String prefixJenaFunctionsTransformations = "trn:";
	public static final String prefixThresholds = "trh:";
	public static final String prefixWeigts = "wght:";
	public static final String prefixSourceAttr = "attrS:";
	public static final String prefixTargetAttr = "attrT:";
	public static final String prefixJenaFunctionsSets = "set:";
	
	public static final String iriRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String iriRDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String iriOWL = "http://www.w3.org/2002/07/owl#";

	public static final String iriJenaFunctionsAggregations = "java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.";
	public static final String iriJenaFunctionsStrings = "java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.";
	public static final String iriJenaFunctionsTransformations = "java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.";
	public static final String iriJenaFunctionsSets = "java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.sets.";

	public static Set<String> usedVariables = Sets.newConcurrentHashSet();
	private static final String ALPHABET = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
	private static final Integer MAX_VARIABLE_LETTERS = 3;
	private static final Integer MAX_VARIABLE_NUMBERS = 3;
		
	public static final String TOKENIZATION_TOKEN = "tokenize##";
	
	
	public static Boolean hasTokenization(String element){
		return element.startsWith(SPARQLFactory.TOKENIZATION_TOKEN);
	}
	
	public static Tuple<String,String> extractTokenization(String element){
		Pattern pattern = Pattern.compile(SPARQLFactory.TOKENIZATION_TOKEN+"\\['.*'\\]");
		Matcher matcher = pattern.matcher(element);
		String matched = "";
		if(matcher.find())
			matched = matcher.group(0);
		int index = element.indexOf(matched)+matched.length();
		Tuple<String,String> tuple = new Tuple<String,String>(matched, element.substring(index));
		return tuple;
	}
	
	
	public static String fixIRIS(String iri){
		
		if(iri.startsWith("http://") || iri.startsWith("<http://") ||  iri.startsWith("java:goal") || iri.startsWith("url:") || iri.startsWith("\"url:")){
			if(!iri.startsWith("<")){
				iri = "<"+iri;
			}
			if(!iri.endsWith(">")){
				iri = iri+">";
			}
		}
		
		return iri;
	}
	
	public static String[] getMainVariable(String query){
	
		StringBuffer queryString = new StringBuffer();
		queryString.append(query);
		if(query.contains("DISTINCT"))
			queryString.replace(queryString.indexOf("DISTINCT"), queryString.indexOf("DISTINCT")+8, "");
		queryString.replace(queryString.indexOf("{"), queryString.length(), "");
		queryString.replace(0, queryString.indexOf("SELECT")+7, "");
		String[] variables = queryString.toString().trim().split(" ");
		
		return variables;
	}
	
	/*
	 * Fix literals
	 */
	
	public static String fixLiterals(String literal){
		String fixedLiteral = literal.trim();
		fixedLiteral = fixedLiteral.replace("\n", "").replace("\\", "\\\\");
		if(fixedLiteral.contains("\""))
			fixedLiteral = fixedLiteral.replace("\"", "'");
		return fixedLiteral;
	}
	
	/*
	 * Translate statements var: attribute to query
	 */

	public static StringBuffer obtainQueryWithContext(Map<String, String> statements, Boolean isSource){
		StringBuffer select = new StringBuffer();
		StringBuffer body = new StringBuffer();
		select.append(obtainQueryHeader());
		String mainVariable = generateFreshVar();
		select.append("SELECT DISTINCT ").append(mainVariable).append(" ");
		
		for(String variable:statements.keySet()){
			String property = statements.get(variable);
				String propertyFixed = fixIRIS(property);	
				body.append("\t").append(mainVariable).append(" ").append(propertyFixed).append(" ").append(variable).append(" .\n");
				if(isSource && select.indexOf(variable) < 0)
					select.append(variable).append(" ");
			
		}
		select.append(" {\n").append(body).append(" ");
		if(isSource){
			select.append("}");
		}else{
			select.append("\t#Bind and Filter\n");
		}
		return select;
	}
	
	
	public static StringBuffer obtainQuery(Map<String, String> statements, Boolean isSource){
		StringBuffer select = new StringBuffer();
		StringBuffer body = new StringBuffer();
		select.append(obtainQueryHeader());
		String mainVariable = generateFreshVar();
		select.append("SELECT DISTINCT ").append(mainVariable).append(" ");
		
		for(String variable:statements.keySet()){
			String property = statements.get(variable);
				String propertyFixed = fixIRIS(property);	
				body.append("\t").append(mainVariable).append(" ").append(propertyFixed).append(" ").append(variable).append(" .\n");
				if(isSource && select.indexOf(variable) < 0)
					select.append(variable).append(" ");
			
		}
		select.append(" {\n").append(body).append(" ");
		if(isSource){
			select.append("}");
		}else{
			select.append("\t#Bind and Filter\n");
		}
		return select;
	}
	
	
	private static StringBuffer obtainQueryHeader(){
		 StringBuffer queryheader = new StringBuffer();
		 queryheader.append("#Prefixes\n");
		 queryheader.append("PREFIX rdf:").append(fixIRIS(iriRDF)).append("\n");
		 queryheader.append("PREFIX rdfs:").append(fixIRIS(iriRDFS)).append("\n");
		 queryheader.append("PREFIX owl:").append(fixIRIS(iriOWL)).append("\n");
		 queryheader.append("PREFIX ").append(prefixJenaFunctionsAggregations).append("<").append(iriJenaFunctionsAggregations).append(">\n");
		 queryheader.append("PREFIX ").append(prefixJenaFunctionsStrings).append("<").append(iriJenaFunctionsStrings).append(">\n");
		 queryheader.append("PREFIX ").append(prefixJenaFunctionsTransformations).append("<").append(iriJenaFunctionsTransformations).append(">\n");
		 queryheader.append("#Query\n");
		 return queryheader;
	 }
	

	 /*
	  * Generate fresh variable
	  */

	public static String generateFreshVar() {
		String freshVar = generateRandomVar();
		
		while(freshVarIsSimilarToUsedOne(freshVar)) {
			freshVar = generateRandomVar();
		}
		usedVariables.add(freshVar);
		return freshVar;
	}
	
	private static Boolean freshVarIsSimilarToUsedOne(String newVar){
		Boolean isSimilar = false;
		for(String oldVar:usedVariables){
			if(oldVar.contains(newVar) || newVar.contains(oldVar)){
				isSimilar = true;
				break;
			}
		}
		return isSimilar;
	}
	 
	 private static String generateRandomVar(){
		 StringBuffer freshVar = new StringBuffer();	 
		 freshVar.append("?");
		 // Setting random number of random letters
		 if(MAX_VARIABLE_LETTERS>0){
			 Integer randomSizeVarLetters = Utils.getRandomInteger(MAX_VARIABLE_LETTERS, 1);
			 for(int i =1; i <= randomSizeVarLetters; i++){
				 Random r = new Random();
				 String randomLetter = String.valueOf(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
				 freshVar.append(randomLetter);
			 }
		 }
		// Setting random number of random numbers
		 if(MAX_VARIABLE_NUMBERS>0){
			 Integer randomSizeVarNumbers = Utils.getRandomInteger(MAX_VARIABLE_NUMBERS, 1);
			 for(int i =1; i <= randomSizeVarNumbers; i++){
				 Integer randomNumber = Utils.getRandomInteger(9, 0);
				 freshVar.append(randomNumber);
			 }
		 }
		 return freshVar.toString();
	 }

	 	 
}
