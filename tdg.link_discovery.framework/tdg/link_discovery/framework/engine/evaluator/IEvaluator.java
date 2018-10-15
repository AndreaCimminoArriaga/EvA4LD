package tdg.link_discovery.framework.engine.evaluator;

import tdg.link_discovery.framework.algorithm.sample.ISampleReader;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;



public interface IEvaluator {

	/**
	 * Sets the environment where the evaluator will evaluate the specifications, i.e.,
	 * the source/target datasets and apealing examples.
	 *
	 * @param  IEnvironment   an Environment object
	 * @return void   	   			
	 */
	public void setExperimentalEnvironment(IEnvironment environment);
	
	/**
	 * Sets the reader used to retrieve the examples from the file specified in the environment
	 *
	 * @param  ISampleReader<?>   reader that returns samples of type ?
	 * @return void   	   			
	 */
	public <T extends Object> void setSampleReader(ISampleReader<T> sampleReader); 
	
	/**
	 * Receives a low-level representation of a specification 
	 * and returns its effectiveness when evaluated agains the examples readed from
	 * the evaluation's environment relying on the setted SampleReader
	 *
	 * @param  Class<? extends Object> object   low-level representation of a specification
	 * @return ConfusionMatrix   	   			obtained confusion matrix
	 */
	public ConfusionMatrix evaluate(Object object);
	
	/**
	 * Receives a low-level representation that is applied to both input 
	 * datasets within the Environment specified. 
	 * <p>
	 * The results are written in an output file setted in the Environment
	 *
	 * @param  Class<? extends Object> object   low-level representation of a specification
	 * @return void   	   			obtained confusion matrix
	 */
	public void apply(Object object);
	
	public IEnvironment getEnvironment();
}
