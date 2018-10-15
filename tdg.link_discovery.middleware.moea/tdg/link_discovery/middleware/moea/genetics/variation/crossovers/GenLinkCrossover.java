package tdg.link_discovery.middleware.moea.genetics.variation.crossovers;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import com.google.common.collect.Lists;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.algorithm.variations.AbstractVariation;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.objects.TreeNode;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;

public class GenLinkCrossover extends AbstractVariation<Tree> implements Variation  {

	protected Integer arity;
	
	public GenLinkCrossover(){
		super(2, 1, 0.75, "GenLinkCrossover");
		this.arity = 2;
	}
	
	public GenLinkCrossover(Double probability){
		super(2, 1, probability, "GenLinkCrossover");
		this.arity = this.inputArity;
	}
	
	@Override
	public int getArity() {
		return this.arity;
	}

	@Override
	public Solution[] evolve(Solution[] parents) {
		Solution[] result = parents.clone();
		Double randomProbability = Utils.getRandomInteger(100, 1)/100.0;
		if( randomProbability<=this.probability){
			Solution s1 = parents[0].copy();
			Solution s2 = parents[1].copy();
				// Translate parent's programs to trees
				LinkSpecification ls1 = (LinkSpecification) s1.getVariable(0);
				LinkSpecification ls2 = (LinkSpecification) s2.getVariable(0);
				List<ISpecification<Tree>> specifications= Lists.newArrayList();
				specifications.add(ls1);
				specifications.add(ls2);
				
				// Cross the trees 
				List<ISpecification<Tree>> offsprings = crossParents(specifications);
				LinkSpecification offspringSpecification = (LinkSpecification) offsprings.get(0);
				s1.setVariable(0, offspringSpecification);
				// Set solutions to return
				result[0] = s1;
				result[1] = s2;
		}
		return result;
	}

	
	@Override
	public List<ISpecification<Tree>> crossParents(List<ISpecification<Tree>> parents) {
		// Retrieve parent's low-level representation
		Tree t1 = parents.get(0).getSpecificationRepresentation();
		Tree t2 = parents.get(1).getSpecificationRepresentation();
		// Cross low-level representation
		Tree treesCrossed = crossTrees(t1, t2);
		// returun offspring
		ISpecification<Tree> offspring = new LinkSpecification(treesCrossed);
		List<ISpecification<Tree>> offsprings = Lists.newArrayList();
		offsprings.add(offspring);
		return offsprings;
	}
	
	
	private Tree crossTrees(Tree tree1, Tree tree2) {
		// Select random crossover operation

		Integer index = Utils.getRandomInteger(7, 1);
		Tree newTree = new Tree();
		if(index == 1){
			newTree = numbersCrossover(tree1, tree2, SPARQLFactory.prefixThresholds);
		}else if(index == 2){
			newTree = numbersCrossover(tree1, tree2, SPARQLFactory.prefixWeigts);
		}else if(index == 3){
			newTree = transformationCrossover(tree1,tree2, SPARQLFactory.prefixJenaFunctionsTransformations);
		}else if(index == 4){
			newTree = swapFunctionsInNodes(tree1,tree2, SPARQLFactory.prefixJenaFunctionsStrings);
		}else if(index == 5){
			newTree = swapFunctionsInNodes(tree1, tree2, SPARQLFactory.prefixJenaFunctionsAggregations);
		}else if(index == 6){
			newTree = operatorsCrossover(tree1, tree2, SPARQLFactory.prefixJenaFunctionsAggregations);
		}else if(index == 7){
			String[] options = new String[]{SPARQLFactory.prefixJenaFunctionsAggregations, SPARQLFactory.prefixJenaFunctionsStrings};
			String option1 = options[Utils.getRandomInteger(options.length-1, 0)];
			String option2 =  options[Utils.getRandomInteger(options.length-1, 0)];
			newTree = hierchayCrossover(tree1, tree2, option1, option2);
		}	
		return newTree;
	}

	
	/*
	 * Threshold and Weight Crossover (avg of their values, changes applied only to tree1)
	 */
	
	private Tree numbersCrossover(Tree tree1, Tree tree2, String prefix) {
		Tree newTree = copyTree(tree1);
		// Retrieve nodes
		Map<String,Integer> tree1Nodes = tree1.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		Map<String,Integer> tree2Nodes = tree2.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		// Check if current link specification have this type of node
		if(tree1Nodes.containsKey(prefix) && tree2Nodes.containsKey(prefix)){
			// Get random nodes of prefix type
			Integer numberOfNodes1 = tree1.getStatistics().get(prefix);
			Integer numberOfNodes2 = tree2.getStatistics().get(prefix);
			Integer randomIndex1 = Utils.getRandomInteger(numberOfNodes1-1, 0);
			Integer randomIndex2 = Utils.getRandomInteger(numberOfNodes2-1, 0);
			// Get numeric nodes from both trees
			Tree subTree1 = tree1.getSubTree(prefix, randomIndex1);
			Tree subTree2  = tree2.getSubTree(prefix, randomIndex2);
			// Compute average
			Double newValue  = 0.5*(Double.valueOf(subTree1.getRoot().toString()) + Double.valueOf(subTree2.getRoot().toString()));
			// Set the new value
			subTree1.setRoot(new TreeNode<String>(newValue.toString()));
			newTree = tree1.replaceSubTreeFromNode(prefix, randomIndex1, subTree1);
		}
		return newTree;
	}
	
	
	/*
	 * Transformations Crossover
	 */

	private Tree transformationCrossover(Tree tree1, Tree tree2, String prefix) {
		Tree newTree = copyTree(tree1);
		// Retrieve nodes
		Map<String,Integer> tree1Nodes = tree1.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		Map<String,Integer> tree2Nodes = tree2.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
	
		// Check if current link specification have this type of node
		if(tree1Nodes.containsKey(prefix) && tree2Nodes.containsKey(prefix)){
			Integer randomIndex1 = Utils.getRandomInteger(tree1Nodes.get(prefix)-1, 0);
			Integer randomIndex2 = Utils.getRandomInteger(tree2Nodes.get(prefix)-1, 0);
			Tree subTree1 = tree1.getSubTree(prefix, randomIndex1);
			Tree subTree2  = tree2.getSubTree(prefix, randomIndex2);
			// Swap transformations comparisons
			Tree subTree1Aux = swapTransformations(subTree1, subTree2);
			newTree = tree1.replaceSubTreeFromNode(prefix, randomIndex1, subTree1Aux);
		}
		
		return newTree;
	}

	
	private Tree swapTransformations(Tree subTree1, Tree subTree2) {
		// Retrieve the subtrees as list of transformations and a property, which is the last element.
		List<TreeNode<String>> elements1 = toListFromTree(subTree1);
		List<TreeNode<String>> elements2 = toListFromTree(subTree2);
		// Retrieve only the transformations
		List<TreeNode<String>> transformations1 = elements1.stream()
					.filter(elem -> elem.getNodeType().equals(SPARQLFactory.prefixJenaFunctionsTransformations))
					.collect(Collectors.toList());
		List<TreeNode<String>> transformations2 = elements2.stream()
				.filter(elem -> elem.getNodeType().equals(SPARQLFactory.prefixJenaFunctionsTransformations))
				.collect(Collectors.toList());

		// Select a random number of transformations (that forms a chain) from both subtrees
		List<TreeNode<String>> randomTransformations = Lists.newArrayList();
		Integer randomIndex = Utils.getRandomInteger(transformations1.size()-1, 0)+1;
		randomTransformations.addAll(transformations1.subList(0, randomIndex));
		randomIndex = Utils.getRandomInteger(transformations2.size()-1, 0)+1;
		randomTransformations.addAll(transformations2.subList(0, randomIndex));

		// Additionally remove duplicates
		randomTransformations = randomTransformations.stream().distinct().collect(Collectors.toList());
		
		// Parse into tree again
		Tree subTree = parseIntoTree(randomTransformations, elements1.get(elements1.size()-1));

		return subTree;
	}

	@SuppressWarnings("unchecked")
	private List<TreeNode<String>> toListFromTree(Tree subTree){
		List<TreeNode<String>> elements =Lists.newArrayList();
		if(subTree.getChilds().isEmpty()){
			elements.add((TreeNode<String>) subTree.getRoot());
		}else{
			elements.add((TreeNode<String>) subTree.getRoot());
			for(Tree child:subTree.getChilds())
				elements.addAll(toListFromTree(child));
		}
		return elements;
 	}
	
	private Tree parseIntoTree(List<TreeNode<String>> randomTransformations, TreeNode<String> treeNode) {
		Tree newTree = new Tree();
		if(randomTransformations.isEmpty()){
			newTree.setRoot(treeNode);
		}else{
			newTree.setRoot(randomTransformations.get(0));
			randomTransformations.remove(0);
			newTree.addChild(parseIntoTree(randomTransformations, treeNode));
		}
		return newTree;
	}


	/*
	 * Aggregate functions and String comparisons Crossover (swap the function in selected nodes, changes applied only to tree1)
	 */
	
	private Tree swapFunctionsInNodes(Tree tree1, Tree tree2, String prefix) {
		Tree newTree = copyTree(tree1);
		// Retrieve nodes
		Map<String,Integer> tree1Nodes = tree1.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		Map<String,Integer> tree2Nodes = tree2.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		// Check if current link specification have this type of node
		if(tree1Nodes.containsKey(prefix) && tree2Nodes.containsKey(prefix)){
			// Get random nodes of prefix type
			Integer numberOfNodes1 = tree1Nodes.get(prefix);
			Integer numberOfNodes2 = tree2Nodes.get(prefix);
			Integer randomIndex1 = Utils.getRandomInteger(numberOfNodes1-1, 0);
			Integer randomIndex2 = Utils.getRandomInteger(numberOfNodes2-1, 0);
			// Get numeric nodes from both trees
			Tree subTree1 = tree1.getSubTree(prefix, randomIndex1);
			Tree subTree2  = tree2.getSubTree(prefix, randomIndex2);
			// Swap string comparisons
			subTree1.setRoot(subTree2.getRoot());
			// Set the new value
			newTree = tree1.replaceSubTreeFromNode(prefix, randomIndex1, subTree1);
		}
		return newTree;
	}
	
	
	/*
	 * Operator crossovers: selects two aggegates, join their childs, remove child if random>0.5
	 */
	
	private Tree operatorsCrossover(Tree tree1, Tree tree2, String prefix) {
		Tree newTree = copyTree(tree1);
		// Retrieve node stats, taking mult nodes in consideration
		Map<String,Integer> tree1Nodes = getStatsWithoutMultNodes(tree1);
		Map<String,Integer> tree2Nodes = getStatsWithoutMultNodes(tree2);
		
		// Check if current link specification have this type of node
		if(tree1Nodes.containsKey(prefix) && tree2Nodes.containsKey(prefix) ){
			// Get random nodes of prefix type, notice that we are not going to take mult nodes into consideration
			Integer numberOfNodes1 = tree1Nodes.get(prefix);
			Integer numberOfNodes2 = tree2Nodes.get(prefix);
			if(numberOfNodes1>0 && numberOfNodes2>0){
				// Obtain non-Mult subtrees and their indexes
				Tuple<Tree, Integer> result1  = getRandomNotMultSubTree(tree1, numberOfNodes1, prefix);
				Tuple<Tree, Integer> result2  = getRandomNotMultSubTree(tree2, numberOfNodes2, prefix);
				
				Tree subTree1 = result1.getFirstElement();
				Integer randomIndex1 = result1.getSecondElement();
				Tree subTree2  = result2.getFirstElement();
				// join childs of aggregates and remove with p>0.5
				Tree auxTree =crossGenLinkOperatorsSubTrees(subTree1, subTree2);
				// Set new tree
				newTree = tree1.replaceSubTreeFromNode(prefix, randomIndex1, auxTree);
			}
		}
	
		return newTree;
	}
	
	private Map<String,Integer> getStatsWithoutMultNodes(Tree tree){
		Map<String,Integer> treeNodes = tree.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		Integer aggregates = treeNodes.get(SPARQLFactory.prefixJenaFunctionsAggregations);
			if(aggregates==null)
				aggregates = 0;
		Integer	multNodes = multOcurrences(tree);
		aggregates = Math.min(aggregates - multNodes, 0);
		treeNodes.put(SPARQLFactory.prefixJenaFunctionsAggregations, aggregates);
		return treeNodes;
	}
	
	private Integer multOcurrences(Tree tree){
		String treeString = tree.toString();
		Pattern pattern = Pattern.compile(SPARQLFactory.iriJenaFunctionsAggregations+"Mult");
        Matcher matcher = pattern.matcher(treeString);
        int counter =  0;
        while(matcher.find()){
        	counter++; 
        }
		return  counter;
	}
	
	private Tuple<Tree, Integer> getRandomNotMultSubTree(Tree tree, Integer numberOfNodes, String prefix){
		Tree subTree = new Tree();
		Integer randomIndex = 0;
		String nodeType = "Mult";
		while(nodeType.contains("Mult")){
			randomIndex = Utils.getRandomInteger(numberOfNodes-1, 0);
			subTree = tree.getSubTree(prefix, randomIndex);				
			nodeType = subTree.getRoot().toString();			
		}
		return new Tuple<Tree,Integer>(subTree, randomIndex);
	}
	
	private Tree crossGenLinkOperatorsSubTrees(Tree subTree1, Tree subTree2){
		Tree auxTree = new Tree();
		auxTree.setRoot(subTree1.getRoot());
		
		// Join childs 
		List<Tree> potentialChilds = Lists.newArrayList();
		potentialChilds.addAll(subTree1.getChilds());
		potentialChilds.addAll(subTree2.getChilds());
		
		// Remove childs with prob > .5
		for(Tree child:potentialChilds){
			if((Utils.getRandomInteger(100, 0)/100.0) > 0.5)
				auxTree.addChild(child);
		}
		
		return auxTree;
	}

	/*
	 * Hierarchy crossover: selects in both trees a random aggregate or cmp and swap them (changes applied only to tree1)
	 */
	
	private Tree hierchayCrossover(Tree tree1, Tree tree2, String prefix1, String prefix2) {
		Tree newTree = copyTree(tree1);
		// Retrieve nodes
		Map<String,Integer> tree1Nodes = tree1.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		Map<String,Integer> tree2Nodes = tree2.getStatistics().entrySet().stream().filter(entry -> entry.getValue()>0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		// Check if current link specification have this type of node
		if(tree1Nodes.containsKey(prefix1) && tree2Nodes.containsKey(prefix2)){
			// Get random nodes of prefix type
			Integer numberOfNodes1 = tree1.getStatistics().get(prefix1);
			Integer numberOfNodes2 = tree2.getStatistics().get(prefix2);
			Integer randomIndex1 = Utils.getRandomInteger(numberOfNodes1-1, 0);
			Integer randomIndex2 = Utils.getRandomInteger(numberOfNodes2-1, 0);
			// Get numeric nodes from both trees
			Tree subTree2  = tree2.getSubTree(prefix2, randomIndex2);
			// Replace in tree1 the selected subtree with subtree selected from tree2
			newTree = tree1.replaceSubTreeFromNode(prefix1, randomIndex1, subTree2);
		}
		return newTree;
	}
	
	
	/*
	 * Others
	 */
	private Tree copyTree(Tree tree){
		Tree newTree = new Tree();
		try{
			newTree = tree.clone();
		}catch(Exception e){
			e.printStackTrace();
		}
		return newTree;
	}


	@Override
	public Boolean hasInputParameters() {
		return true;
	}

	@Override
	public Map<String, Double> getInputDefaultParameters() {
		Map<String,Double> defaultParameters = Maps.newHashMap();
		defaultParameters.put("crossover_rate", 0.75);
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
