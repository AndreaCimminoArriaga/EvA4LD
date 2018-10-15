package tdg.link_discovery.framework.engine.translator;

import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.objects.Tuple;

public interface ITranslator {

	/**
	 * Receives a high-level representation of a specification 
	 * and returns a low-level representation of the same object
	 *
	 * @param  <T extends Specification>   any object that extends a specification
	 * @return <T extends Object>    	   any object that encodes the low-level representation
	 */
	public Object translate(ISpecification<?> specification);
	
	/**
	 * Receives a high-level representation of a specification 
	 * and returns a low-level representation of the same object. May use the environment to set dataset restrictions
	 *
	 * @param  <T extends Specification>   any object that extends a specification
	 * @return <T extends Object>    	   any object that encodes the low-level representation
	 */
	public Object translate(ISpecification<?> specification, IEnvironment environment);

	
}
