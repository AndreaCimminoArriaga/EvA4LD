package tdg.link_discovery.tools;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;

public class toRemove2 {

	private static List<Integer> population = Lists.newArrayList();
	private static List<Integer> iterations = Lists.newArrayList();
	private static List<Double> crossrate = Lists.newArrayList();
	private static List<Double> mutatrate = Lists.newArrayList();
	private static List<String> csv = Lists.newArrayList();
	private static Integer counter = 0;
	public static void main(String[] args) {
		population.add(20);
		population.add(100);
		population.add(500);
		iterations.add(20);
		iterations.add(50);
		iterations.add(100);
		crossrate.add(0.25);
		crossrate.add(0.5);
		crossrate.add(0.75);
		mutatrate.add(0.25);
		mutatrate.add(0.5);
		mutatrate.add(0.75);
		
		List<Setup> lines = Lists.newArrayList();
		List<String> algorithms = Lists.newArrayList();
		algorithms.add("moea-gen1");
		algorithms.add("moea-gen2");
		algorithms.add("moea-gen3");
		for(String name:algorithms) {
			counter = 0;
			for(Integer pop:population) {
				for(Integer it:iterations) {
					if(pop==20 && it >20) {
						lines.addAll(createSetup( it, pop, name ));
					}else if(pop==100) {
						lines.addAll(createSetup( it, pop, name ));
					}else if(pop == 500 && it< 100) {
						lines.addAll(createSetup( it, pop, name ));
					}
				}
			}
			
		}
		try {
		FileUtils.writeLines(new File("setups.csv"), csv);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static List<Setup> createSetup(Integer iterations, Integer population, String name) {
		List<Setup> setups = Lists.newArrayList();
		for(Double cross:crossrate) {
			for(Double mut:mutatrate) {
				Setup setup = new Setup();
				setup.addVariables(1);
				setup.addObjectives(1);
				setup.addGenerationsRate(10);
				
				if(name.equals("moea-gen1") || name.equals("moea-gen2") ) {
					setup.addParentsSelectionSize(5);
				}else {
					setup.addParentsSelectionSize(2);
				}
				setup.addMaxIterations(iterations);
				setup.addPopulationSize(population);
				
				if(cross==0.25 && mut >0.4) {
					setup.addCrossoverRate(cross);
					setup.addMutationRate(mut);
					setups.add(setup);
					createSetupFile(setup, name);
				}else if(cross==0.5) {
					setup.addCrossoverRate(cross);
					setup.addMutationRate(mut);
					setups.add(setup);
					createSetupFile(setup, name);
				}else if(cross == 0.75 && mut< 0.6) {
					setup.addCrossoverRate(cross);
					setup.addMutationRate(mut);
					setups.add(setup);
					createSetupFile(setup, name);
				}
				
			}
		}
		return setups;
	}

	
	private static void createSetupFile(Setup setup, String name) {
		File folder = new File("./setups/");
		folder.mkdirs();
		File newSetup = new File("./setups/"+name+"_setup"+counter+".cnf");
		csv.add("\""+counter+"\",\""+name+"_setup"+counter+".cnf\",\""+setup.getGenerationsRate()+"\",\""+setup.getMaxIterations()+"\",\""+setup.getPopulationSize()+"\",\""+setup.getCrossoverRate()+"\",\""+setup.getMutationRate()+"\",\""+setup.getParentsSelectionSize()+"\"");
		counter++;

		try {
			if(!newSetup.exists())
				newSetup.createNewFile();
			Files.write(setup.toString().getBytes(), newSetup);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
