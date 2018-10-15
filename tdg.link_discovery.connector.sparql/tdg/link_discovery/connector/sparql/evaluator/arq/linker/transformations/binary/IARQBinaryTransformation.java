package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.binary;

import tdg.link_discovery.framework.learner.functions.TransformationFunction;
import tdg.link_discovery.middleware.objects.Tuple;

public interface IARQBinaryTransformation extends TransformationFunction {

	public Tuple<String,String> applyBinaryTransformation(String element1, String element2);
}
