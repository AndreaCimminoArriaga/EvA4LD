package tdg.link_discovery.middleware.moea.genetics.problem_statement;

import java.util.List;
import java.util.Map;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import com.google.common.collect.Maps;

import tdg.link_discovery.framework.algorithm.fitness.IFitness;
import tdg.link_discovery.framework.algorithm.individual.initializer.ISpecificationInitializer;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.utils.Utils;

public class LinkSpecificationDiscovery extends AbstractProblem{

	protected List<IFitness> fitnessFunctions; // Evaluate the solution's variables
	protected IEngine engine; 				   // Execute link specification represented as sparql queries over the scenarios
	protected ISpecificationInitializer linkSpecificationCreator; // Allows the creation of different random link specifications relying on several methods
	public static Map<String,Solution> solutionsCache = Maps.newConcurrentMap();

	
	public LinkSpecificationDiscovery(Integer variables, Integer objectives, IEngine engine, ISpecificationInitializer linkSpecificationCreator, List<IFitness> fitnessFunctions) {
		super(variables, objectives);
		this.fitnessFunctions = fitnessFunctions;
		this.engine = engine;
		this.linkSpecificationCreator = linkSpecificationCreator;

	}
	
	@Override
	public void evaluate(Solution solution) {
		
		// Retrieve Tree representation of a link specification
		LinkSpecification ls = (LinkSpecification) solution.copy().getVariable(0);
		if(solutionsCache.containsKey(ls.toString())) {
			Solution solutionCached = solutionsCache.get(ls.toString());
			double[] objectives = solutionCached.getObjectives();
			solution.setObjectives(objectives);
		}else {
			// Evaluate solutions
			ConfusionMatrix confusionMatrix = this.engine.evaluate((LinkSpecification)ls.copy());
			int objectiveIndex = 0;
			for(IFitness function:this.fitnessFunctions){
				Double rawScore = function.evaluateSolutionResults(confusionMatrix, new Object[]{ls});
				Double evaluationResult = Utils.roundDecimal(1 - rawScore, FrameworkConfiguration.DECIMAL_PRECISION);
				//System.out.println(Utils.roundDecimal(confusionMatrix.getFMeasure(),2)+" "+evaluationResult+"  :  "+confusionMatrix);
				evaluationResult = Utils.roundDecimal(evaluationResult, FrameworkConfiguration.DECIMAL_PRECISION);
				solution.setObjective(objectiveIndex, evaluationResult);
				objectiveIndex++;
				solutionsCache.put(ls.toString(), solution.copy());
			}
			//System.out.println(solution.getObjectives()[0]+" => "+ls.getLinkSpecificationTree());
		}
	}

	@Override
	public Solution newSolution() {
		Solution sol = new Solution(this.numberOfVariables,this.numberOfObjectives);
		LinkSpecification linkSpecification = (LinkSpecification) linkSpecificationCreator.createLinkSpecification();
		sol.setVariable(0, linkSpecification);
		return sol;
	}

}
