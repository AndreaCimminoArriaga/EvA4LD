package tdg.link_discovery.middleware.framework.learner.executor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.IARQAggregate;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.IARQStringSimilarity;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.IARQUnaryTransformation;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.utils.ClassesUtils;
import tdg.link_discovery.middleware.utils.ObjectUtils;
import tdg.link_discovery.middleware.utils.StreamUtils;

public class LinkSpecificationExecutor implements Callable<Double>{

	protected String expression;
	
	
	public LinkSpecificationExecutor(String expression) {
		this.expression=expression;
	}
		
	@Override
	public Double call() throws Exception {
		return evaluateStringSpecification(this.expression);
	}


	public Double evaluateStringSpecification(String expression) {
		
		Double score = 0.0;
		// Solve transformations
		String noTransformationExpression = solveTransformationFunctions(expression.replace(FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER, FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER));
		// Solve string comparisons
		String noStringComparisons = solveStringComparisons(noTransformationExpression);
		// Iterate Solving aggregations
		String noAggregations = noStringComparisons;
		int iterations = 0;
		boolean error = false;
		while(noAggregations.contains(SPARQLFactory.prefixJenaFunctionsAggregations)) {
			noAggregations = solveAggregates(noAggregations);
			iterations++;
			if(iterations>100) {
				System.out.println("Error with: "+noAggregations);
				error = true;
				break;
			}
				
		}
		if(!error)
			score = Double.valueOf(removeWrapString(noAggregations,FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER));
		return score;
	}
	
	private String solveAggregates(String expression) {
		// Create a Pattern object
		Pattern regex = Pattern.compile(SPARQLFactory.prefixJenaFunctionsAggregations + "[a-zA-Z0-9]+\\([∂0-9\\.\\-,\\sE]+\\)");
		// Now create matcher object.
		Matcher matcher = regex.matcher(expression);
		String newExpression = expression;
		while (matcher.find()) {
			// Extract transformation function
			String originalFunction = matcher.group();
			// Separate function name and arguments
			String functionAux = originalFunction.replaceAll("[a-z]+:", "").replace(")", "");
			String functionName = functionAux.substring(0, functionAux.indexOf("("));
			// Prepare arguments, e.g., separate them and remove \"
			String[] functionArgument = functionAux.substring(functionAux.indexOf("(")+1, functionAux.length()).trim().split(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER + ",");
			List<Double> arguments = StreamUtils.asStream(functionArgument).map(value -> Double.valueOf(removePartialWrapString(value,
							FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER)))
					.collect(Collectors.toList());
			// Create string comparison object and apply the function
			IARQAggregate aggregateFunction = (IARQAggregate) ObjectUtils.createObjectByName(
					ClassesUtils.findClassPackageByName(functionName), new Class[] {}, new Object[] {});
			Double result = aggregateFunction.applyAggregation(arguments);
		
			// Replace in the expression the result
			newExpression = newExpression.replace(originalFunction, wrapString(result.toString(), FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER));
		}
		
		return newExpression;
	}

	private String solveStringComparisons(String expression) {
		// Create a Pattern object
		Pattern regex = Pattern.compile(SPARQLFactory.prefixJenaFunctionsStrings+ "[a-zA-Z0-9]+\\(∂[^∂]+∂,∂[^∂]+∂,[^\\)]+\\)");
		// Now create matcher object.
		Matcher matcher = regex.matcher(expression);
		String newExpression = expression;
		while (matcher.find()) {
			// Extract transformation function
			String originalFunction = matcher.group();
			// Separate function name and arguments
			String functionAux = originalFunction.replaceAll("[a-z]+:", "").replace(")", "");
			String functionName = functionAux.substring(0, functionAux.indexOf("("));
			// Prepare arguments, e.g., separate them and remove \"
			String[] functionArgument = functionAux.substring(functionAux.indexOf("(")+1, functionAux.length()).trim().split(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER+","); 
			List<String> arguments = StreamUtils.asStream(functionArgument).map(value -> removePartialWrapString(value,FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER)).collect(Collectors.toList());
			// Create string comparison object and apply the function
			IARQStringSimilarity stringComparisonFunction = (IARQStringSimilarity) ObjectUtils.createObjectByName(ClassesUtils.findClassPackageByName(functionName), new Class[] {}, new Object[] {});
			Double result = stringComparisonFunction.similarity(arguments.get(0), arguments.get(1), Double.valueOf(arguments.get(2)));
			// Replace in the expression the result
			newExpression = newExpression.replace(originalFunction, wrapString(result.toString(), FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER));
		}
		return newExpression;
	}

	private String solveTransformationFunctions(String expression) {
		// Create a Pattern object
		Pattern regex = Pattern.compile(SPARQLFactory.prefixJenaFunctionsTransformations+"[a-zA-Z0-9]+[^\\)]+\\)");
		// Now create matcher object.
		Matcher matcher = regex.matcher(expression);
		String newExpression = expression;
		while (matcher.find()) {
			// Extract transformation function
			String originalFunction = matcher.group();
			// Separate function name and arguments
			String functionAux = originalFunction.replaceAll("[a-z]+:", "").replace(")", "");
			String[] functionElements = functionAux.split("\\(");
			String functionName = functionElements[0].trim();
			String functionArgument = removeWrapString(functionElements[1].trim(), FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
			// Create transformation object and apply the function
			IARQUnaryTransformation transformationFunction = (IARQUnaryTransformation) ObjectUtils.createObjectByName(ClassesUtils.findClassPackageByName(functionName), new Class[] {}, new Object[] {});
			String result = transformationFunction.applyUnaryTransformation(functionArgument);
			// Replace in the expression the result
			newExpression = newExpression.replace(originalFunction, wrapString(result, FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER));
		}
		return newExpression;
	}

	
	
	// Adds a prefix and a suffix to a given string
	private String wrapString(String value, String separator) {
			StringBuffer newValue = new StringBuffer();
			newValue.append(separator);
			newValue.append(value);
			newValue.append(separator);
			return newValue.toString();
	}
	// remove wrap 	
	private String removeWrapString(String literal, String separator) {
		String newLiteral = literal;
		if(literal.startsWith(separator) && literal.endsWith(separator))
			newLiteral = newLiteral.substring(separator.length(), newLiteral.lastIndexOf(separator));
		return newLiteral;
	}
	
	// remove wrap
	private String removePartialWrapString(String literal, String separator) {
		String newLiteral = literal;
		if (newLiteral.startsWith(separator))
			newLiteral = newLiteral.substring(separator.length(), newLiteral.length());
		if (newLiteral.endsWith(separator))
			newLiteral = newLiteral.substring(0, newLiteral.lastIndexOf(separator));
		return newLiteral;
	}




}
