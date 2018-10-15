package tdg.link_discovery.framework.gui.desktop;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Sets;

import tdg.link_discovery.middleware.utils.ClassesUtils;

public class GuiConfiguration {
	
	public static final String GUI_DATABASE_FILE = "./gui/data/h2";


	public static final String DATA_DIRECTORY = "./tdb-data/";
	public static final String ALGORTIHMS_DIRECTORY = "./experiments/algorithms/";
	public static final String ENVIRONMENTS_DIRECTORY = "./experiments/environments/";
	
	
	public static final String INITIALIZERS_PACKAGES_IN_FILE = "./conf/genetic/initializer.cnf";
	public static final String SELECTORS_PACKAGES_IN_FILE = "./conf/genetic/selector.cnf";
	public static final String REPLACEMENTS_PACKAGES_IN_FILE = "./conf/genetic/replacement.cnf";
	public static final String FITNESS_PACKAGES_IN_FILE = "./conf/genetic/fitness.cnf";
	public static final String CROSSOVERS_PACKAGES_IN_FILE = "./conf/genetic/crossover.cnf";
	public static final String MUTATIONS_PACKAGES_IN_FILE = "./conf/genetic/mutation.cnf";

	public static final String ATTRIBUTE_LEARNERS_PACKAGES_IN_FILE = "./conf/algorithms/attribute_learners.cnf";
	public static final String AGGREGATES_PACKAGES_IN_FILE = "./conf/functions/aggregates.cnf";
	public static final String METRICS_PACKAGES_IN_FILE = "./conf/functions/metrics.cnf";
	
	public static final String TRANSFORMATIONS_PACKAGES_IN_FILE= "./conf/functions/transformations.cnf";
	
	
	public static Set<String> initializersClasses = loadPackagesDirectoriesListsFromFile(INITIALIZERS_PACKAGES_IN_FILE);
	public static Set<String> selectorsClasses = loadPackagesDirectoriesListsFromFile(SELECTORS_PACKAGES_IN_FILE);
	public static Set<String> replacementsClasses = loadPackagesDirectoriesListsFromFile(REPLACEMENTS_PACKAGES_IN_FILE);
	public static Set<String> fitnessClasses = loadPackagesDirectoriesListsFromFile(FITNESS_PACKAGES_IN_FILE);
	public static Set<String> crossoversClasses = loadPackagesDirectoriesListsFromFile(CROSSOVERS_PACKAGES_IN_FILE);
	public static Set<String> mutationsClasses = loadPackagesDirectoriesListsFromFile(MUTATIONS_PACKAGES_IN_FILE);
	public static Set<String> aggregatesClasses = loadPackagesDirectoriesListsFromFile(AGGREGATES_PACKAGES_IN_FILE);
	public static Set<String> attributeLearnerClasses = loadPackagesDirectoriesListsFromFile(ATTRIBUTE_LEARNERS_PACKAGES_IN_FILE);
	public static Set<String> stringMetricsClasses = loadPackagesDirectoriesListsFromFile(METRICS_PACKAGES_IN_FILE);
	public static Set<String> transfromationsClasses = loadPackagesDirectoriesListsFromFile(TRANSFORMATIONS_PACKAGES_IN_FILE);
	
	
	

	private static Set<String> loadPackagesDirectoriesListsFromFile(String packageName) {
			Set<String> clazzes = Sets.newHashSet();
			try{
				Stream<String> lines = Files.lines(Paths.get(packageName), Charset.defaultCharset());
				lines.forEachOrdered(packhage -> clazzes.addAll(ClassesUtils.listClassesFromPackage(packhage)));
				lines.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			return clazzes;
		}

}
