package tdg.link_discovery.middleware.moea.genetics.variation.crossovers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.algorithm.variations.AbstractVariation;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;

public class SubtreeCrossover extends AbstractVariation<Tree> implements Variation {

	/*
	 * TREE CROSSOVER: it selects a branch from two trees and swaps the branches.
	 */	

	private int arity;
	private Map<String, String> compatibleNodes;
	

	public SubtreeCrossover(){
		super(2, 2, 0.5, "tree.crossover");
		this.arity = 2;
		compatibleNodes = Maps.newHashMap();
		setCompatibleNodesToSwap();
	}
	
	public SubtreeCrossover(Double probability){
		super(2, 2, probability, "tree.crossover");
		this.arity = this.inputArity;
		compatibleNodes = Maps.newHashMap();
		setCompatibleNodesToSwap();
	}

	private void setCompatibleNodesToSwap(){
		StringBuilder str = new StringBuilder();
		// Aggregates & string comparisons
		str.append(SPARQLFactory.prefixJenaFunctionsAggregations).append("@").append(SPARQLFactory.prefixJenaFunctionsStrings);
		compatibleNodes.put(SPARQLFactory.prefixJenaFunctionsAggregations, str.toString());
		compatibleNodes.put(SPARQLFactory.prefixJenaFunctionsStrings, str.toString());
		
		// Numbers
		compatibleNodes.put(SPARQLFactory.prefixThresholds, SPARQLFactory.prefixThresholds);
		compatibleNodes.put(SPARQLFactory.prefixWeigts, SPARQLFactory.prefixWeigts);
		
		// Tansformations & attributes
		str = new StringBuilder();
		str.append(SPARQLFactory.prefixSourceAttr).append("@").append(SPARQLFactory.prefixTargetAttr);
		compatibleNodes.put(SPARQLFactory.prefixJenaFunctionsTransformations, str.toString());
		
		str = new StringBuilder();
		str.append(SPARQLFactory.prefixSourceAttr).append("@").append(SPARQLFactory.prefixJenaFunctionsTransformations);
		compatibleNodes.put(SPARQLFactory.prefixSourceAttr, str.toString());
		str = new StringBuilder();
		str.append(SPARQLFactory.prefixTargetAttr).append("@").append(SPARQLFactory.prefixJenaFunctionsTransformations);
		compatibleNodes.put(SPARQLFactory.prefixTargetAttr, str.toString());		
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
		Double randomProbability = Utils.getRandomInteger(100, 1)/100.0;
		if( randomProbability<=this.probability){
				Solution s1 = parents[0].copy();
				Solution s2 = parents[1].copy();
				// Retrieving parents
				LinkSpecification ls1 = (LinkSpecification) s1.getVariable(0);
				LinkSpecification ls2 = (LinkSpecification) s2.copy().getVariable(0);
				List<ISpecification<Tree>> genitors = Lists.newArrayList();
				genitors.add(ls1);
				genitors.add(ls2);
				// Crossing parents
				List<ISpecification<Tree>> offspring = crossParents(genitors);
				// Setting children as solution
				LinkSpecification children1 = (LinkSpecification) offspring.get(0);
				LinkSpecification children2 = (LinkSpecification) offspring.get(1);
				// Modifiying solutions
				s1.setVariable(0, children1);
				s1.setObjective(0, 0.0);
				s2.setVariable(0, children2);
				s2.setObjective(0, 0.0);
				result[0] = s1;
				result[1] = s2;
		}
		return result;
	}
	
	
	@Override
	public List<ISpecification<Tree>> crossParents(List<ISpecification<Tree>> parents) {
		LinkSpecification ls1 = (LinkSpecification) parents.get(0);
		LinkSpecification ls2 = (LinkSpecification) parents.get(1);
		// Cross the trees
		Tuple<Tree,Tree> treesCrossed = crossTrees(ls1.getLinkSpecificationTree(),
												   ls2.getLinkSpecificationTree());
		// Set solutions to return
		Tree tree1Crossed = treesCrossed.getFirstElement();
		Tree tree2Crossed = treesCrossed.getSecondElement();
		
		parents.get(0).setSpecificationRepresentation(tree1Crossed);
		parents.get(1).setSpecificationRepresentation(tree2Crossed);
		return parents;
	}
	
	
	
	

	private  Tuple<Tree,Tree> crossTrees(Tree tree1, Tree tree2){
		// Select from both trees compatible nodes Agg:Agg or Cmp, Cmp: Agg or Cmp....
		String compatibleNode = getRandomNode(tree1, tree2);
		Tuple<String,Integer> nodeFixedToSwap1 = getRandomNodeToSwap(tree1, compatibleNode);
		Tuple<String,Integer> nodeFixedToSwap2 = getRandomNodeToSwap(tree2, compatibleNode);
		
		
		// Retrieve subtree to swap
		Tree subTreeSource = tree1.getSubTree(nodeFixedToSwap1.getFirstElement(), nodeFixedToSwap1.getSecondElement());
		Tree subTreeTarget = tree2.getSubTree(nodeFixedToSwap2.getFirstElement(), nodeFixedToSwap2.getSecondElement());
		
		// Swap subtrees
		Tree newT1 = tree1.replaceSubTreeFromNode(nodeFixedToSwap1.getFirstElement(), nodeFixedToSwap1.getSecondElement(), subTreeTarget);
		Tree newT2 = tree2.replaceSubTreeFromNode(nodeFixedToSwap2.getFirstElement(), nodeFixedToSwap2.getSecondElement(), subTreeSource);
		
		Tuple<Tree,Tree> crossedTrees = new Tuple<Tree,Tree>(newT1, newT2);
		return crossedTrees;
	}
	

	
	private String getRandomNode(Tree tree1, Tree tree2){
		// Retrieve operators from trees
		Map<String,Integer> tree1Nodes = tree1.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		Map<String,Integer> tree2Nodes = tree2.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		// Selecting one in common
		Set<String> commonNodes = Sets.intersection(tree1Nodes.keySet(), tree2Nodes.keySet());
		String randomCommonNode = (String) Utils.getRandomElement(commonNodes);
			
		return randomCommonNode;
	}
	
	private Tuple<String,Integer> getRandomNodeToSwap(Tree tree, String selectedNode){
		Map<String,Integer> treeNodes = tree.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		String randomNode = "";
		// Check from the compatible nodes if selectedNode has other compatibles
		while(!treeNodes.containsKey(randomNode)){
			String[] posibilities = this.compatibleNodes.get(selectedNode).split("@");
			// In case it has, then we select one option randomly
			Integer randomIndex = Utils.getRandomInteger(posibilities.length-1, 0);
			randomNode = posibilities[randomIndex];
		}
		// Finally we select a random node index from the tree 
		Integer randomIndex = Utils.getRandomInteger(treeNodes.get(randomNode)-1, 0);
		Tuple<String,Integer> selected = new Tuple<String,Integer>(randomNode, randomIndex);
		
		return selected;
	}


	@Override
	public Boolean hasInputParameters() {
		return true;
	}

	@Override
	public Map<String, Double> getInputDefaultParameters() {
		Map<String,Double> defaultParameters = Maps.newHashMap();
		defaultParameters.put("crossover_rate", 0.5);
		defaultParameters.put("crossover_input_arity", 2.0);
		defaultParameters.put("crossover_output_arity", 1.0);
		return defaultParameters;
	}

	@Override
	public void setInputParameters(String parameterName, Object value) {
		if(parameterName.equals("crossover_rate"))
			setProbability((Double) value);
		if(parameterName.equals("input_arity"))
			setInputArity((Integer) value);
		if(parameterName.equals("output_arity"))
			setOuputArity((Integer) value);
		
	}
	
	
	

}
