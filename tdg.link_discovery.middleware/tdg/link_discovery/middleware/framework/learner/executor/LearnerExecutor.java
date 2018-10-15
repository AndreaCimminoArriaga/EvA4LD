package tdg.link_discovery.middleware.framework.learner.executor;

import java.util.List;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.engine.SparqlEngine;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.ILearner;
import tdg.link_discovery.middleware.framework.learner.builder.LearnerBuilder;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.tools.effectiveness.LinkageEvaluator;

public class LearnerExecutor {
	
	private static ILearner learner;
	private static Integer numberOfExecutions, currentExecution;
	public static Boolean executionFinished;
	
	public LearnerExecutor(){
		learner = null;
		numberOfExecutions= 0;
		executionFinished = true;
	}

	public void executeLearner(LearnerBuilder builder, IEnvironment environment, Integer executionsNumber){
		int executions_counter = 1;
		String outputFile = environment.getLinksOutput();
		numberOfExecutions = executionsNumber;
		executionFinished = false;
		while(executions_counter <= executionsNumber){
			// Learning
			currentExecution = executions_counter-1;
			learner = builder.getLearner();
			learner.learnSpecifications();
			Set<String> processed = Sets.newHashSet();
			List<ISpecification<Tree>> specifications = learner.getBestLearnedSpecifications();
			// Linking
			for(ISpecification<Tree> specification:specifications){
				
				if(!processed.contains(specification.toString())){
					// Modify output name
					environment.setLinksOutput(outputFile.replace(".nt", executions_counter+".nt"));
					// Apply specification
					long startTime = System.currentTimeMillis();
					//----
					IEngine engine = builder.getEngine();
					engine.linkData(specification);
					
					//----
					long stopTime = System.currentTimeMillis();
				    long elapsedTime = stopTime - startTime;
					// Evaluate ouptut
					
					// Update vars
					processed.add(specification.toString());
					executions_counter++;
				}
				
				if(executions_counter>executionsNumber)
					break;
				break;
			}
		}		
		executionFinished = true;
	}
	
	public static Double getExecutionProgress(){
		if(numberOfExecutions==null || learner==null)
			return 0.0;
		
		return (100*currentExecution)+learner.getAlgorithmProgress()/((numberOfExecutions-1)*100);
	}
	
	public Boolean hasExecutorFinishLearning(){
		return executionFinished;
	}
}
