package tdg.link_discovery.connector.sparql.algorithm.initializer;

import java.util.Map;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.individual.initializer.AbstractSpecificationInitializer;
import tdg.link_discovery.framework.learner.functions.FunctionsFactory;
import tdg.link_discovery.framework.learner.functions.StringMetricFunction;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.objects.TreeNode;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;

public class UnaryTreeGrowCreator extends AbstractSpecificationInitializer {
	
	
	
	public UnaryTreeGrowCreator(){
		super("UnaryTreeGrow Initializer");
	
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
		// Create the tree recursively
		Tree newLinkSpecificationTree = buildComparisonTree(FunctionsFactory.getRandomSimilarity(), FunctionsFactory.getRandomSuitableAttribute());
		// Create the object link specification
		LinkSpecification linkSpecification = new LinkSpecification(newLinkSpecificationTree);
		return linkSpecification;
	}
	
	

	private Tree buildComparisonTree(StringMetricFunction strCmp, Tuple<String, String> attrPair){
		Double thresholdValue = Utils.getRandomInteger(100, 0)/100.0;
		
		// Init nodes
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
		
		
		
		return comparison;
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
