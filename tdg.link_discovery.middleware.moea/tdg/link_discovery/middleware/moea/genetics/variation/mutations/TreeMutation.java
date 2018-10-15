package tdg.link_discovery.middleware.moea.genetics.variation.mutations;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.algorithm.variations.AbstractVariation;
import tdg.link_discovery.framework.learner.functions.FunctionsFactory;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.objects.TreeNode;
import tdg.link_discovery.middleware.utils.Utils;

public class TreeMutation extends AbstractVariation<Tree> implements Variation {
	//implements Variation
	/*
	 * TREE Mutation: it selects a random node in the tree and replaces it
	 * with another random node that has the same nodeType.
	 */


	private int arity;
	
	public TreeMutation() {
		super(1, 1, 0.5, "tree.mutation");
		this.arity = 1;
	}
	
	public TreeMutation(Double probability) {
		super(1, 1, probability, "tree.mutation");
		this.arity = this.inputArity;
	}

	public void setArity(int arity) {
		this.arity = arity;
	}

	@Override
	public int getArity() {
		return arity;
	}

	@Override
	public Solution[] evolve(Solution[] parents) {
		Solution[] result = parents.clone();
		Double randomIndex = (Utils.getRandomInteger(100, 1)/100.0);
		if(randomIndex<=this.probability){
			Solution currentChromosome = parents[0].copy();
			LinkSpecification ls = (LinkSpecification) currentChromosome.getVariable(0);
			
			List<ISpecification<Tree>> mutatedSpecifications = crossParents(Lists.newArrayList(ls));
			LinkSpecification mutatedLs = (LinkSpecification) mutatedSpecifications.get(0);
			currentChromosome.setVariable(0, mutatedLs);
			currentChromosome.setObjective(0, 0.0);
			
			result[0] = currentChromosome;
			result[1] = parents[1].copy();
		
		}
		
		return result;
	}
	
	@Override
	public List<ISpecification<Tree>> crossParents(List<ISpecification<Tree>> parents) {
		// Retrieving subject to mutate
		LinkSpecification ls = (LinkSpecification) parents.get(0);
		// selecting node to mutate
		Map<String, Integer> treeStatistics = ls.getStatistics()
				.entrySet()
				.stream().filter(entry -> entry.getValue() >0)
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
							
		String randomNodeType = (String) Utils.getRandomElement(treeStatistics.keySet());
		Integer randomNodeIndex = Utils.getRandomInteger(treeStatistics.get(randomNodeType), 1);
		// Muting ls
		mute(ls.getLinkSpecificationTree(), randomNodeType, randomNodeIndex);	
		// Return mutated ls
		List<ISpecification<Tree>> result = Lists.newArrayList();
		result.add(ls);
		return result;
	}
	
	
	/*
	 * Change node
	 */
	private int changingNodeIndex = 0;
	private void mute(Tree tree, String nodeType, Integer nodeIndex){
		changingNodeIndex = 0;
		recursiveChangeNode(tree, nodeType, nodeIndex);		
	}
	
	public void recursiveChangeNode(Tree tree, String nodeType, Integer index){
		String currentNodeType = tree.getRoot().getNodeType();	
		
		if(nodeType.equals(currentNodeType))
			changingNodeIndex++;
		
		if(nodeType.equals(currentNodeType) && index == changingNodeIndex){
			// Base case: modify current node
			String operator = getRandomOperator(currentNodeType);
			TreeNode<String> newNode = new TreeNode<String>(operator);
			newNode.setNodeType(currentNodeType);
			tree.setRoot(newNode);
		}else{
			// Recursive case: keep looking for node
			for(Tree child:tree.getChilds()){
				recursiveChangeNode(child, nodeType, index);
			}
		}
	}

	
	
	public String getRandomOperator(String nodeType){
		StringBuilder operator = new StringBuilder();
		if(nodeType.equals(SPARQLFactory.prefixWeigts) || nodeType.equals(SPARQLFactory.prefixThresholds)){
			// Numeric value
			Double newNumber = Utils.roundDecimal(Utils.getRandomInteger(100, 0)/100.0, FrameworkConfiguration.DECIMAL_PRECISION);
			operator.append(String.valueOf(newNumber));
		}else if(nodeType.equals(SPARQLFactory.prefixSourceAttr)){
			// Src attr
			operator.append(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER)
					.append(FunctionsFactory.getRandomSuitableAttribute().getFirstElement())
					.append(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		}else if(nodeType.equals(SPARQLFactory.prefixTargetAttr)){
			// Trg attr
			operator.append(FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER)
					.append(FunctionsFactory.getRandomSuitableAttribute().getSecondElement())
					.append(FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
		}else if(nodeType.equals(SPARQLFactory.prefixJenaFunctionsTransformations)){
			// Transformations
			operator.append(FunctionsFactory.getRandomTransformation().getName());
		}else if(nodeType.equals(SPARQLFactory.prefixJenaFunctionsStrings)){
			// Str Comparison
			operator.append(FunctionsFactory.getRandomSimilarity().getName());
		}else if(nodeType.equals(SPARQLFactory.prefixJenaFunctionsAggregations)){
			// Str Comparison
			operator.append(FunctionsFactory.getRandomAggregate().getName());
		}
		return operator.toString();
	}

	@Override
	public Boolean hasInputParameters() {
		return true;
	}

	@Override
	public Map<String, Double> getInputDefaultParameters() {
		Map<String,Double> defaultParameters = Maps.newHashMap();
		defaultParameters.put("mutation_rate", 0.5);
		defaultParameters.put("crossover_input_arity", 2.0);
		defaultParameters.put("crossover_output_arity", 1.0);
		return defaultParameters;
	}

	@Override
	public void setInputParameters(String parameterName, Object value) {
		if(parameterName.equals("mutation_rate"))
			setProbability((Double) value);
		if(parameterName.equals("input_arity"))
			setInputArity((Integer) value);
		if(parameterName.equals("output_arity"))
			setOuputArity((Integer) value);
		
	}
	
}
