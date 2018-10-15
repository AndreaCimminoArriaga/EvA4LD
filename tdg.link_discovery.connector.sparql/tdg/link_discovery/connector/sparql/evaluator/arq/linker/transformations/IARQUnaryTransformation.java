package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations;


import tdg.link_discovery.framework.learner.functions.TransformationFunction;


public interface IARQUnaryTransformation extends TransformationFunction{

	public String applyUnaryTransformation(String element);
}
