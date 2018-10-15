package tdg.link_discovery.middleware.framework.configuration;

import java.nio.file.Paths;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.tdb.setup.StoreParams;
import org.apache.jena.tdb.setup.StoreParamsCodec;

import tdg.link_discovery.middleware.log.Logger;

public class FrameworkConfiguration {
	
	public static String LINKED_INSTANCES_OUTPUT_FILE = "output.nt";
	public static Integer DECIMAL_PRECISION = 4;
	public static Integer MAX_LINK_SPECIFICATION_DEPTH = 5; 
	public static Integer MIN_LINK_SPECIFICATION_DEPTH = 3; // The minimum is 3, e.g., (0 agg):[ (1 cmp):[(2 attr),(2 attr),(2 thre)], (1 weight)]
	public static Integer MAX_LINK_SPECIFICATION_BREADTH = 5; //  The minimum is 1
	public static Integer MIN_LINK_SPECIFICATION_BREADTH = 1; //  The minimum is 1
	
	// LinkSpecification as Variable
	public static String LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER = "∂";
	public static String LINK_SPECIFICATION_TARGET_ATTR_DELIMITER = "ß";

	// Setup
	public static final String SETUP_SEPARATOR_TOKEN = ":=";
	//	- Environment setup
	public static final String ENVIRONMENT_SOURCEDATASET_INFILE_TOKEN = "source_dataset";
	public static final String ENVIRONMENT_TARGETDATASET_INFILE_TOKEN = "target_dataset";
	public static final String ENVIRONMENT_EXAMPLESFILE_INFILE_TOKEN = "examples_file";
	public static final String ENVIRONMENT_SPECIFICATIONS_OUTPUT_FILE_INFILE_TOKEN = "specifications_output";
	public static final String ENVIRONMENT_LINKS_OUTPUT_FILE_INFILE_TOKEN = "links_output";
	public static final String ENVIRONMENT_SOURCE_RESTRICTIONS_INFILE_TOKEN = "source_class_restrictions";
	public static final String ENVIRONMENT_TARGET_RESTRICTIONS_INFILE_TOKEN = "target_class_restrictions";
	public static final String ENVIRONMENT_SUITABLE_ATTRIBUTES_INFILE_TOKEN  = "suitable_attributes";
	public static final String ENVIRONMENT_GOLD_STANDARD_INFILE_TOKEN  = "gold_standard";
	public static final String ENVIRONMENT_ALGORITHM_STATISTICS_INFILE_TOKEN = "algorithm_statistics_file";
	
	public static final String SETUP_MAX_ITERATION_INFILE_TOKEN = "max_iterations";
	public static final String SETUP_POPULATION_SIZE_INFILE_TOKEN = "population_size";
	public static final String SETUP_NUM_ALGORITHM_OBJECTIVES_INFILE_TOKEN = "objectives_num";
	public static final String SETUP_NUM_AGLORITHM_VARIABLES_INFILE_TOKEN = "variables_num";
	public static final String SETUP_CROSSOVER_RATE_INFILE_TOKEN = "crossover_rate";
	public static final String SETUP_MUTATION_RATE_INFILE_TOKEN  = "mutation_rate";
	public static final String SETUP_GENERATIONS_RATE_INFILE_TOKEN  = "generations_rate";
	public static final String SETUP_PARENTS_SELECTION_RATE_INFILE_TOKEN  = "selector_arity";
	
	public static final String ALGORITHM_NAME_INFILE_TOKEN = "name"; 
	public static final String ALGORITHM_INITIALIZATION_INFILE_TOKEN = "initializer_class"; 
	public static final String ALGORITHM_SELECTOR_INFILE_TOKEN = "selector_class"; 
	public static final String ALGORITHM_CROSSOVER_INFILE_TOKEN = "crossover_class"; 
	public static final String ALGORITHM_MUTATION_INFILE_TOKEN = "mutation_class"; 
	public static final String ALGORITHM_FITNESS_INFILE_TOKEN = "fitness_class"; 
	public static final String ALGORITHM_REPLACEEMT_INFILE_TOKEN = "replacement_class"; 
	public static final String ALGORITHM_ATTRIBUTE_LEARNER_INFILE_TOKEN = "attribute_learner_class"; 
	public static final String ALGORITHM_ENVINE_INFILE_TOKEN = "engine_class"; 
	public static final String ALGORITHM_STRING_METRICS_INFILE_TOKEN = "string_metrics_classes"; 
	public static final String ALGORITHM_AGGREGATES_INFILE_TOKEN = "aggregate_classes"; 
	public static final String ALGORITHM_TRANSFORMATIONS_INFILE_TOKEN = "transformation_classes";
	

	public static Boolean REDUCE_SEARCH_SPACE_METRICS = false;
	public static Boolean APPLY_STRING_TRANSFORMATIONS_BLOCK = false;
	
	
	public static final String FRAMEWORK_WORKSPACE_DIRECTORY = Paths.get("./experiments").toString(); 
	public static final String FRAMEWORK_ALGORITHMS_DIRECTORY = Paths.get("./experiments/algorithms").toString(); 
	public static final String FRAMEWORK_ENVIRONMENT_DIRECTORY = Paths.get("./experiments/environments").toString(); 
	public static final String FRAMEWORK_GOLD_STANDARD_DIRECTORY = Paths.get("./experiments/gold-stds").toString(); 
	public static final String FRAMEWORK_EXECUTION_LOG_DIRECTORY = Paths.get("./experiments/").toString(); 
	
	
	public static Logger traceLog;
	public static Logger resultsLog;
	public static boolean TERMINAL_LOG = true;
	
	public static StoreParams getTDBSetup() {
		String xs = "{ \n" + 
				"  \"tdb.file_mode\" :               \"mapped\" ,\n" + 
				"  \"tdb.block_size\" :              16384 ,\n" + 
				"  \"tdb.block_read_cache_size\" :   10000000,\n" + 
				"  \"tdb.block_write_cache_size\" :  20000000 ,\n" + 
				"  \"tdb.node2nodeid_cache_size\" :  10000000 ,\n" + 
				"  \"tdb.nodeid2node_cache_size\" :  50000000 ,\n" + 
				"  \"tdb.node_miss_cache_size\" :    10000 ,\n" + 
				"  \"tdb.index_node2id\" :           \"node2id\" ,\n" + 
				"  \"tdb.index_id2node\" :           \"nodes\" ,\n" + 
				"  \"tdb.triple_index_primary\" :    \"SPO\" ,\n" + 
				"  \"tdb.triple_indexes\" :          [ \"SPO\" ] ,\n" + 
				"  \"tdb.quad_index_primary\" :      \"GSPO\" ,\n" + 
				"  \"tdb.quad_indexes\" :            [ \"GSPO\" , \"GPOS\" , \"GOSP\" , \"POSG\" , \"OSPG\" , \"SPOG\" ] ,\n" + 
				"  \"tdb.prefix_index_primary\" :    \"GPU\" ,\n" + 
				"  \"tdb.prefix_indexes\" :          [ \"GPU\" ] ,\n" + 
				"  \"tdb.file_prefix_index\" :       \"prefixIdx\" ,\n" + 
				"  \"tdb.file_prefix_nodeid\" :      \"prefix2id\" ,\n" + 
				"  \"tdb.file_prefix_id2node\" :     \"prefixes\"\n" + 
				"}";
		 JsonObject x = JSON.parse(xs);
		 StoreParams paramsActual = StoreParamsCodec.decode(x);
		 return paramsActual;
	}
	
	// H2 setup
	public static String SOURCE_H2_DATABASE = "h2_data";
	public static String TARGET_H2_DATABASE = "h2_data";
	
	
}
