package tdg.link_discovery.connector.sparql.algorithm.initializer;

import java.util.Map;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Mult;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.individual.initializer.AbstractSpecificationInitializer;
import tdg.link_discovery.framework.learner.functions.AggregateFunction;
import tdg.link_discovery.framework.learner.functions.FunctionsFactory;
import tdg.link_discovery.framework.learner.functions.StringMetricFunction;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.objects.TreeNode;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;

public class TreeGrowCreator extends AbstractSpecificationInitializer {
	
	private Boolean randomWeights;
	
	public TreeGrowCreator(){
		super("TreeGrow Initializer");
		randomWeights = true;
	}
	
	
	@Override
	public String encloseSourceAttribute(String sourceAttribute) {
		StringBuilder attributeStringBuilder = new StringBuilder();
		attributeStringBuilder.append(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		attributeStringBuilder.append(sourceAttribute);
		attributeStringBuilder.append(FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER);
		return attributeStringBuilder.toString();
	}


	@Override
	public String encloseTargetAttribute(String targetAttribute) {
		StringBuilder attributeStringBuilder = new StringBuilder();
		attributeStringBuilder.append(FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
		attributeStringBuilder.append(targetAttribute);
		attributeStringBuilder.append(FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER);
		return attributeStringBuilder.toString();
	}

	@Override
	public LinkSpecification createLinkSpecification() {
		// Setup parameters
		Integer max_depth = FrameworkConfiguration.MAX_LINK_SPECIFICATION_DEPTH;
		Integer min_depth = FrameworkConfiguration.MIN_LINK_SPECIFICATION_DEPTH;
		Integer current_depth = 0;
		
		// Create the tree recursively
		Tree newLinkSpecificationTree = recursiveTreeCreation(max_depth, min_depth, current_depth);
		// Create the object link specification
		LinkSpecification linkSpecification = new LinkSpecification(newLinkSpecificationTree);
		return linkSpecification;
	}
	
	
	private Tree recursiveTreeCreation(Integer max_depth, Integer min_depth, Integer current_depth){
		Tree tree = null;
		Integer depthBoundary = max_depth - min_depth;	
		if(current_depth < depthBoundary){
				// Generate random tree for this recursive call
				Tuple<Tree,Boolean> randomTree = createRandomTree();
				tree = randomTree.getFirstElement();
				// Case Base: random tree created is a node mult. Mult has 3 layers of nodes below: (0 agg):[ (1 cmp):[(2 attr),(2 attr),(2 thre)], (1 weight)]
				// Recursive case: 
				if(randomTree.getSecondElement() != true){
					// Generate a random number of child nodes in the range MAX_BREADTH, MIN_BREADTH 
					Integer min_breadth = FrameworkConfiguration.MIN_LINK_SPECIFICATION_BREADTH;
					Integer max_breadth = FrameworkConfiguration.MAX_LINK_SPECIFICATION_BREADTH;
					Integer breadth = Utils.getRandomInteger(max_breadth, min_breadth);
					
					for(int index=min_breadth; index<=breadth; index++){
						Tree childTree = recursiveTreeCreation(max_depth, min_depth, current_depth++);
						if(childTree!=null)
							tree.addChild(childTree);
					}
				}
				
		}else if(current_depth == depthBoundary){
			// Case base: max depth reached
			tree = createComparisonTree(FunctionsFactory.getRandomSimilarity());
		}
		return tree;
	}
	
	private Tuple<Tree,Boolean> createRandomTree(){
		Tree newTree = new Tree();
		Boolean isTerminal = false;
		if((Utils.getRandomInteger(100, 1)/100.0) <= 0.5){
			newTree = createComparisonTree(FunctionsFactory.getRandomSimilarity());
			isTerminal = true;
		}else{
			TreeNode<String> node = createAggregateTree();
			newTree.setRoot(node);	
		}
		return new Tuple<Tree, Boolean>(newTree, isTerminal);
	}
		
	private TreeNode<String> createAggregateTree(){
		// Retrieve random aggregate
		AggregateFunction aggregate = FunctionsFactory.getRandomAggregate();
		// Gerante agg node
		TreeNode<String> node = new TreeNode<String>(aggregate.getName());
		node.setNodeType(SPARQLFactory.prefixJenaFunctionsAggregations);
		
		return node;
	}
	
	private Tree createComparisonTree(StringMetricFunction strCmp){
		Mult multAggregate = new Mult();
		Double  weight = 1.0;
		if(randomWeights)
			weight = Utils.getRandomInteger(100, 0) / 100.0;
		if(weight.isNaN() || weight.isInfinite())
			weight = 0.0;
		Tuple<String, String> attrPair = FunctionsFactory.getRandomSuitableAttribute();
		
		return buildComparisonTree(multAggregate, strCmp, weight, attrPair);
		
	}

	private Tree buildComparisonTree(Mult multAggregate, StringMetricFunction strCmp, Double  weight, Tuple<String, String> attrPair){
		Double thresholdValue = Utils.getRandomInteger(100, 0)/100.0;
		
		// Init nodes
		TreeNode<String> root = new TreeNode<String>(multAggregate.getName());
		root.setNodeType(SPARQLFactory.prefixJenaFunctionsAggregations);
		TreeNode<String> weightChild = new TreeNode<String>(String.valueOf(Utils.roundDecimal(weight, FrameworkConfiguration.DECIMAL_PRECISION)));
		weightChild.setNodeType(SPARQLFactory.prefixWeigts);
		TreeNode<String> stringCmpChild = new TreeNode<String>(strCmp.getName());
		stringCmpChild.setNodeType(SPARQLFactory.prefixJenaFunctionsStrings);
		TreeNode<String> attrSource = new TreeNode<String>(encloseSourceAttribute(attrPair.getFirstElement()));
		attrSource.setNodeType(SPARQLFactory.prefixSourceAttr);
		TreeNode<String> attrTarget = new TreeNode<String>(encloseTargetAttribute(attrPair.getSecondElement()));
		attrTarget.setNodeType(SPARQLFactory.prefixTargetAttr);
		TreeNode<String> threshold = new TreeNode<String>(String.valueOf(Utils.roundDecimal(thresholdValue, FrameworkConfiguration.DECIMAL_PRECISION)));
		threshold.setNodeType(SPARQLFactory.prefixThresholds);
				
		// Create comparison nodes
		Tree comparison = new Tree();
		comparison.setRoot(stringCmpChild);
		
		comparison.addChild(attrSource);
		comparison.addChild(attrTarget);
		comparison.addChild(threshold);
		
		// Pack the comparison with a weight
		Tree resultTree = new Tree();
		resultTree.setRoot(root);
		resultTree.addChild(comparison);
		resultTree.addChild(weightChild);
		
		return resultTree;
	}

	@Override
	public Map<String, Double> getInputDefaultParameters() {
		return null;
	}


	@Override
	public Boolean hasInputParameters() {
		return false;
	}


	@Override
	public void setInputParameters(String parameterName, Object value) {
		// empty	
	}

	
	
}
