package tdg.link_discovery.connector.h2.procedures;

import java.util.List;
import java.util.stream.Collectors;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Max;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.*;
import tdg.link_discovery.middleware.utils.StreamUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class Procedures {


	/*
	 * Max
	 */
	public static Double max(String numbers) {
		List<Double> values = StreamUtils.asStream(numbers.split(",")).map(str -> Double.valueOf(str.trim())).collect(Collectors.toList());
		Max max = new Max();
		return max.applyAggregation(values);
	}
	
	
	/*
	 * Jaro similarity
	 */
	public static String jaroSimilarity(String value1, String value2, Double threshold) {
		IARQStringSimilarity jaro = new JaroSimilarity();
		Double score = jaro.similarity(value1, value2, threshold);
		return String.valueOf(Utils.roundDecimal(score, 2));
	}
	
	/*
	 * Cosine similarity
	 */
	public static String cosineSimilarity(String value1, String value2, Double threshold) {
		IARQStringSimilarity cosine = new CosineSimilarity();
		Double score = cosine.similarity(value1, value2, threshold);
		return String.valueOf(Utils.roundDecimal(score, 2));
	}
	
	

}
