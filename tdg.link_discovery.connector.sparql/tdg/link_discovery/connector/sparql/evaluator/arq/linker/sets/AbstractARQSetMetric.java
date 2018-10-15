package tdg.link_discovery.connector.sparql.evaluator.arq.linker.sets;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase4;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.utils.Utils;

public abstract class AbstractARQSetMetric extends FunctionBase4 implements IARQSetMetric{

	protected final String name;
	protected Integer decimalPrecision;
	
	public AbstractARQSetMetric(String name){
		StringBuffer str = new StringBuffer();
		str.append(SPARQLFactory.prefixJenaFunctionsSets).append(name);
		this.name = str.toString();
		System.out.println(this.name);
		decimalPrecision = FrameworkConfiguration.DECIMAL_PRECISION;
	}


	@Override
	public NodeValue exec(NodeValue arg0, NodeValue arg1, NodeValue arg2, NodeValue arg3) {
		String scoresArraysString = arg0.getString();
		Integer sourceSize = arg1.getInteger().intValue();
		Integer targetSize = arg2.getInteger().intValue();
		Double threshold = arg3.getDouble();
		
		List<Integer> valuesAdjusted = getNumberOfLinks(scoresArraysString, sourceSize, targetSize);
		Integer links = valuesAdjusted.get(0);
		sourceSize = valuesAdjusted.get(1);
		targetSize = valuesAdjusted.get(2);
		Double score = applySetMetric(links, sourceSize, targetSize);
		
		if(threshold < 0 && score > 0){
			// 'exist' threhold
			score = 1.0;
		}else{
			// numeric threshold
			if(score ==  1.0){
				score = 1.0;
			}else if(score == threshold){
				score = 0.01;
			}else if(score > threshold){
				score = (score-threshold) / (1-threshold);
			}else{
				score = (score/threshold)-1;
			}
		}
			
		
		
		score = Utils.roundDecimal(score, SPARQLFactory.DECIMAL_PRECISION);
		
		
		System.out.println("|S n T|="+links);
		System.out.println("|S|="+sourceSize);
		System.out.println("|T|="+targetSize);
		System.out.println(" µ="+score);
		
		NodeValue resultantNode = NodeValue.makeDouble(0.9);
		return resultantNode;
	
	}

	
	/*
	 * For each source instance we retrieve a set of neighbours: pi
	 * 
	 * In the sparql query we obtain a score for any comparison of a pi with
	 * the neighbours in the target dataset, i.e., for a paper in the source p1 we obtain
	 * the resultant sets of scores of comparing it with 6 articles: forall p1: s1, s2, s3, s4, s5, s6
	 * 
	 * Then for all pi we obtain the same scoring array. The lenght of the array is the target cardinality set,
	 * the source cardinality set is received as argument.
	 * 
	 * For any array of scores, if a number is higher than 0 it means is a link, however, take into account that if more than
	 * one number has a value higher it means that one source instance was linked with several in the target dataset. hence the target
	 * cardinality has to be adjusted taking this number into account.
	 * 
	 * On the other hand, it may happen that a several source instances were linked with one target instance, we can
	 * identify this if two arrays have a value higher than 1 in the same position; which would mean that the instance in such position was linked
	 * several times. Hence taking this into account the source cardinality has to be reduced
	 */
	private List<Integer> getNumberOfLinks(String scoresArraysString, Integer sourceSize, Integer targetSize){
		List<String> scores = Arrays.asList(scoresArraysString.split("@")).stream().collect(Collectors.toList());
		Integer links = 0;
		Integer targetAdjustement = 0;
		List<Integer> sourcePositiveScoresPosition = Lists.newArrayList();
		Boolean firstIteration = true;
		// Check if inside an array there is more than one position with score higher than one
		for(int index =0; index <scores.size(); index++){
			List<Double> score = Arrays.asList(scores.get(index).split(",")).stream().map(num -> Double.valueOf(num)).collect(Collectors.toList());
			if(firstIteration){
				sourcePositiveScoresPosition = fillInitial(score.size(), 0);
				firstIteration=false;
			}
			Integer positiveScores = (int) score.stream().filter(num -> num>0).count();
			if(positiveScores>0)
				links++;
			if(positiveScores>1)
				targetAdjustement += positiveScores - 1;
			// Save positions where score was higher than 1 in the aggregate array: sourcePositiveScoresPosition
			List<Integer> scoresAbsolutes = score.stream().map(num -> transformScore(num)).collect(Collectors.toList());
			sourcePositiveScoresPosition = sumVectors(sourcePositiveScoresPosition, scoresAbsolutes);
		}
		
		Integer sourceAdjustement = sourcePositiveScoresPosition.stream().filter(num -> num>1).mapToInt(i -> i.intValue()).sum();
		Integer adjustedSource = sourceSize - sourceAdjustement;
		Integer adjustedTarget = targetSize - targetAdjustement;
		List<Integer> adjustements = Lists.newArrayList();
		adjustements.add(links);
		adjustements.add(adjustedSource);
		adjustements.add(adjustedTarget);
		return adjustements;
	}

	private Integer transformScore(Double score){
		if(score>0)
			return 1;
		return 0;
	}
	
	private List<Integer> sumVectors(List<Integer> vector1, List<Integer> vector2){
		List<Integer> result = Lists.newArrayList();
		for(int index =0; index <vector1.size(); index++){
			result.set(index, vector1.get(index)+ vector1.get(index));
		}
		return result;
	}
	
	private List<Integer> fillInitial(Integer size, Integer value){
		List<Integer> generatedList = Lists.newArrayList();
		for(int index =0; index <value; index++){
			generatedList.set(index, value);
		}
		return generatedList;
	}
	
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractARQSetMetric other = (AbstractARQSetMetric) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	


}
