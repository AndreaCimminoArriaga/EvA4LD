package tdg.link_discovery.framework.engine;

import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.evaluator.IEvaluator;
import tdg.link_discovery.framework.engine.translator.ITranslator;
import tdg.link_discovery.framework.objects.FrameworkObject;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;

public interface IEngine extends FrameworkObject{

	/**
	 * Sets the translator that converts specification in high-level representations into low-level representations
	 *
	 * @param  ITranslator  translator
	 * @return void
	 */
	public void setTranslator(ITranslator  translator);
	
	/**
	 * Sets the evaluator that is able to evaluate a low-level representation
	 *
	 * @param  IEvaluator evaluator 
	 * @return void   	   	
	 */
	public void setEvaluator(IEvaluator evaluator);
	
	/**
	 * Given a high-level representation of a specification, it returns the efficiency achieved by the specification over several examples
	 * defined in an Evironment object setted in the Evaluator class of this Engine object
	 *
	 * @param  Class<? extends Specification> specification   high-leve representation of a specification
	 * @return ConfusionMatrix   	   						  obtained confusion matrix
	 */
	public <T extends Object> ConfusionMatrix evaluate(ISpecification<T> specification);
	
	
	/**
	 * Applies to the dasets defined in the Environment related to this engine the given input high-level representation of a specification.
	 * The results are written in the ouput file defined in the Environment 
	 *
	 * @param ISpecification   high-leve representation of a specification
	 * @return void
	 */
	public <T extends Object> void linkData(ISpecification<T> specification);

	
	@Override
	public String getName();
	@Override
	public void setName(String name);
}
