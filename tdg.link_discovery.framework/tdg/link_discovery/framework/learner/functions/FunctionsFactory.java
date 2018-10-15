package tdg.link_discovery.framework.learner.functions;


import java.util.List;

import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;

import com.google.common.collect.Lists;

public class FunctionsFactory {

	public static List<AggregateFunction> aggregateFunctions = Lists.newArrayList();
	public static AggregateFunction aggregateMult = null;
	public static List<StringMetricFunction> similarityFunctions = Lists.newArrayList();
	
	public static List<TransformationFunction> transformationsFunctions = Lists.newArrayList();
	public static List<Tuple<String,String>> suitableAttributes = Lists.newArrayList();
	
	
	
	public static List<Tuple<String,String>> getAttributes(){
		return suitableAttributes;
	}
	
	public static Tuple<String,String> getRandomSuitableAttribute(){
		Tuple<String,String> suitableAttribute = null;
		if(!suitableAttributes.isEmpty()){
			Integer randomIndex = Utils.getRandomInteger(suitableAttributes.size()-1, 0);
			suitableAttribute = suitableAttributes.get(randomIndex);
		}
		return suitableAttribute;
	}
	
	public static StringMetricFunction getRandomSimilarity(){
		StringMetricFunction randomSimilarity = null;
		if(!similarityFunctions.isEmpty()){
			Integer randomIndex = Utils.getRandomInteger(similarityFunctions.size()-1, 0);
			randomSimilarity = similarityFunctions.get(randomIndex);
		}
		return randomSimilarity;
	}
	
	public static AggregateFunction getRandomAggregate(){
		AggregateFunction randomAggregate= null;
		if(!aggregateFunctions.isEmpty()){
			Integer randomIndex = Utils.getRandomInteger(aggregateFunctions.size()-1, 0);
			randomAggregate= aggregateFunctions.get(randomIndex);
		}
		return randomAggregate;
	}
	
	public static TransformationFunction getRandomTransformation(){
		TransformationFunction randomTransformation = null;
		if(!transformationsFunctions.isEmpty()){
			Integer randomIndex = Utils.getRandomInteger(transformationsFunctions.size()-1, 0);
			randomTransformation= transformationsFunctions.get(randomIndex);
		}
		return randomTransformation;
	}

	public static AggregateFunction getMultFunction() {
		return aggregateMult;
	}

	
	
	
	
	
	
	

	
	
	
}
