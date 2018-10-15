package tdg.link_discovery.tools;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class toRemove3 {

	public static Set<String> alreadyExistant = Sets.newConcurrentHashSet();
	
	
	public static void main(String[] args) {
		alreadyExistant.add(getGenlinkConfig());
		alreadyExistant.add(getCarvalhoConfig());
		alreadyExistant.add(getEagleConfig());
		Set<String> newConfigs = Sets.newHashSet();
		
		List<String> initializers = Lists.newArrayList();
		initializers.add("tdg.link_discovery.connector.sparql.algorithm.initializer.UnaryTreeGrowCreator");
		initializers.add("tdg.link_discovery.connector.sparql.algorithm.initializer.TreeGrowCreator");
		initializers.add("tdg.link_discovery.connector.sparql.algorithm.initializer.GenLinkCreator");
		
		List<String> selectors = Lists.newArrayList();
		selectors.add("tdg.link_discovery.middleware.moea.algorithm.selector.RouletteWheelSelector");
		selectors.add("tdg.link_discovery.middleware.moea.algorithm.selector.TournamentSelector");
			
		List<String> crossovers = Lists.newArrayList();
		crossovers.add("tdg.link_discovery.middleware.moea.genetics.variation.crossovers.GenLinkCrossover");
		crossovers.add("tdg.link_discovery.middleware.moea.genetics.variation.crossovers.SubtreeCrossover");
		
		List<String> mutations = Lists.newArrayList();
		mutations.add("tdg.link_discovery.middleware.moea.genetics.variation.mutations.GenLinkMutation");
		mutations.add("tdg.link_discovery.middleware.moea.genetics.variation.mutations.TreeMutation");
		
		List<String> fitness = Lists.newArrayList();
		fitness.add("tdg.link_discovery.middleware.moea.genetics.fitness_function.FMeasureFitness");
		fitness.add("tdg.link_discovery.middleware.moea.genetics.fitness_function.FmeasureGenlink");
		
		List<String> replacements = Lists.newArrayList();
		replacements.add("tdg.link_discovery.middleware.framework.algorithm.replacement.UPlusBReplacement");
		replacements.add("tdg.link_discovery.middleware.framework.algorithm.replacement.GenerationalReplacement");
		replacements.add("tdg.link_discovery.middleware.framework.algorithm.replacement.RandomReplacement");
		
		for(String initializer:initializers) {
			for(String selector:selectors) {
				for(String crossover:crossovers) {
					for(String mutation:mutations) {
						for(String fitnes:fitness) {
							for(String replacement:replacements) {
								StringBuffer newConfig = new StringBuffer();
								newConfig.append(initializer).append(",").append(selector).append(",");
								newConfig.append(crossover).append(",").append(mutation).append(",");
								newConfig.append(fitnes).append(",").append(replacement);
								newConfigs.add(newConfig.toString());
							}
						}
					}
				}
			}
		}
		
		System.out.println(newConfigs.size());
	}
	
	
	private static String getGenlinkConfig() {
		StringBuffer strbuff= new StringBuffer();
		strbuff.append("").append(",");
		
		return strbuff.toString();
	}
	
	private static String getCarvalhoConfig() {
		StringBuffer strbuff= new StringBuffer();
		strbuff.append("").append(",");
		
		return strbuff.toString();
	}
	
	private static String getEagleConfig() {
		StringBuffer strbuff= new StringBuffer();
		strbuff.append("").append(",");
		
		return strbuff.toString();
	}
	
}
