package tdg.link_discovery.connector.sparql.algorithm.initializer;

import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Maps;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Mult;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.IARQStringSimilarity;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.algorithm.individual.initializer.AbstractSpecificationInitializer;
import tdg.link_discovery.framework.learner.functions.AggregateFunction;
import tdg.link_discovery.framework.learner.functions.FunctionsFactory;
import tdg.link_discovery.framework.learner.functions.StringMetricFunction;
import tdg.link_discovery.framework.learner.functions.TransformationFunction;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.objects.TreeNode;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;

public class GenLinkCreator extends AbstractSpecificationInitializer{

	private Double DOUBLE_COMPARISON_PROBABILITY;
	private Double TRANSFORMATIONS_PROBABILITY;

	
	public GenLinkCreator(){
		super("Genlink Initializer");
		DOUBLE_COMPARISON_PROBABILITY = 0.5;
		TRANSFORMATIONS_PROBABILITY = 0.5;
	}
	
	@Override
	public ISpecification<Tree> createLinkSpecification() {
		AggregateFunction aggegation =  FunctionsFactory.getRandomAggregate();
		StringMetricFunction stringCmp =  FunctionsFactory.getRandomSimilarity();
		StringMetricFunction stringCmp2 = null;
		
		if((Utils.getRandomInteger(100, 1)/100.0) < DOUBLE_COMPARISON_PROBABILITY){
			// Generate second comparison
			stringCmp2 = FunctionsFactory.getRandomSimilarity();
		}
		
		Tree comparison1 = createComparisonTree(stringCmp);
		Tree comparison2 = null;
		if(stringCmp2!=null)
			comparison2 = createComparisonTree(stringCmp);
		
		Tree aggregate = createAggregateTree(aggegation, comparison1, comparison2);
		LinkSpecification linkSpecification = new LinkSpecification(aggregate);
		
		return linkSpecification;
	}
	
	private Tree createAggregateTree(AggregateFunction aggegation, Tree comparison1,Tree comparison2){
		TreeNode<String> root = new TreeNode<String>(aggegation.getName());
		root.setNodeType(SPARQLFactory.prefixJenaFunctionsAggregations);
		Tree aggregateTree = new Tree();
		aggregateTree.setRoot(root);
		aggregateTree.addChild(comparison1);
		if(comparison2!=null){
			aggregateTree.addChild(comparison2);
		}
		
		return aggregateTree;
	}

	private Tree createComparisonTree(StringMetricFunction strCmp){
		Mult multAggregate = new Mult();
		Double  weigh = Utils.getRandomInteger(100, 0) / 100.0;
		Tuple<String, String> attrPair = FunctionsFactory.getRandomSuitableAttribute();
		TransformationFunction transformationSource = null;
		TransformationFunction transformationTarget = null;
		
		if((Utils.getRandomInteger(100, 1)/100.0) <= TRANSFORMATIONS_PROBABILITY){
			transformationSource = FunctionsFactory.getRandomTransformation();
		}
		
		if((Utils.getRandomInteger(100, 1)/100.0) <= TRANSFORMATIONS_PROBABILITY){
			transformationTarget = FunctionsFactory.getRandomTransformation();
		}
		
		return buildComparisonTree(multAggregate, strCmp, weigh, attrPair, transformationSource, transformationTarget);
		
	}

	private Tree buildComparisonTree(Mult multAggregate, StringMetricFunction strCmp, Double  weigh, Tuple<String, String> attrPair, TransformationFunction transformationSource,  TransformationFunction transformationTarget){
		Double thresholdValue = Utils.getRandomInteger(100, 0)/100.0;
		
		// Init nodes
		TreeNode<String> root = new TreeNode<String>(multAggregate.getName());
		root.setNodeType(SPARQLFactory.prefixJenaFunctionsAggregations);
		TreeNode<String> weightChild = new TreeNode<String>(weigh.toString());
		weightChild.setNodeType(SPARQLFactory.prefixWeigts);
		TreeNode<String> stringCmpChild = new TreeNode<String>(strCmp.getName());
		stringCmpChild.setNodeType(SPARQLFactory.prefixJenaFunctionsStrings);
		TreeNode<String> attrSource = new TreeNode<String>(encloseSourceAttribute(attrPair.getFirstElement()));
		attrSource.setNodeType(SPARQLFactory.prefixSourceAttr);
		TreeNode<String> attrTarget = new TreeNode<String>(encloseTargetAttribute(attrPair.getSecondElement()));
		attrTarget.setNodeType(SPARQLFactory.prefixTargetAttr);
		TreeNode<String> threshold = new TreeNode<String>(thresholdValue.toString());
		threshold.setNodeType(SPARQLFactory.prefixThresholds);
		
		// Add transformation nodes
		Tree attributeSourceTree = new Tree();
		if(transformationSource!=null){
			TreeNode<String> transNode = new TreeNode<String>(transformationSource.getName());
			transNode.setNodeType(SPARQLFactory.prefixJenaFunctionsTransformations);
			attributeSourceTree.setRoot(transNode);
			attributeSourceTree.addChild(attrSource);
		}else{
			attributeSourceTree.setRoot(attrSource);
		}
		Tree attributeTargetTree = new Tree();
		if(transformationTarget!=null){
			TreeNode<String> transNode = new TreeNode<String>(transformationTarget.getName());
			transNode.setNodeType(SPARQLFactory.prefixJenaFunctionsTransformations);
			attributeTargetTree.setRoot(transNode);
			attributeTargetTree.addChild(attrTarget);
		}else{
			attributeTargetTree.setRoot(attrTarget);
		}
		// Create comparison nodes
		Tree comparison = new Tree();
		comparison.setRoot(stringCmpChild);
		comparison.addChild(attributeSourceTree);
		comparison.addChild(attributeTargetTree);
		comparison.addChild(threshold);
		
		// Pack the comparison with a weight
		Tree resultTree = new Tree();
		resultTree.setRoot(root);
		resultTree.addChild(comparison);
		resultTree.addChild(weightChild);
		
		return resultTree;
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
	public Map<String, Double> getInputDefaultParameters() {
		Map<String,Double> defaultParameters = Maps.newHashMap();
		defaultParameters.put("initializer_comparison_creation_rate", 0.5);
		defaultParameters.put("initializer_transformations_creation_rate", 0.5);
		return defaultParameters;
	}


	@Override
	public Boolean hasInputParameters() {
		return true;
	}


	@Override
	public void setInputParameters(String parameterName, Object value) {	
		if(parameterName.equals("initializer_comparison_creation_rate"))
			DOUBLE_COMPARISON_PROBABILITY = (Double) value;
		if(parameterName.equals("initializer_transformations_creation_rate"))
			TRANSFORMATIONS_PROBABILITY = (Double) value;
		 
	}


}
