package tdg.moea.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.tdb.setup.StoreParams;
import org.apache.jena.tdb.setup.StoreParamsCodec;
import com.google.common.collect.Lists;

import tdg.link_discovery.connector.h2.engine.H2Engine;
import tdg.link_discovery.connector.h2.engine.translator.H2Translator;
import tdg.link_discovery.connector.h2.linker.H2Linker;
import tdg.link_discovery.connector.sparql.engine.SparqlEngine;
import tdg.link_discovery.connector.sparql.engine.evaluator.KFoldEvaluator;
import tdg.link_discovery.connector.sparql.engine.evaluator.SparqlEvaluator;
import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.KFoldDumperEvaluator;
import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.KFoldEvaluatorInMemory;
import tdg.link_discovery.connector.sparql.engine.evaluator.linker.Linker;
import tdg.link_discovery.connector.sparql.engine.evaluator.linker.deprecated.LinkerInMemoryModel;
import tdg.link_discovery.connector.sparql.engine.sample_reader.NTSampleReader;
import tdg.link_discovery.connector.sparql.engine.translator.SparqlTranslator;
import tdg.link_discovery.connector.sparql.engine.translator.StringTranslator;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.framework.engine.evaluator.IEvaluator;
import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.framework.engine.translator.ITranslator;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.environment.Environments;
import tdg.link_discovery.middleware.log.Logger;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.objects.TreeNode;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;

public class ManualLinkSpec {
	
	
	public static void main(String[] args) {
		
		
		Tree attributeT = new Tree();
		attributeT.setRoot(new TreeNode<String>("∂http://schema.org/creator∂"));
		Tree attributeS = new Tree();
		//attributeS.setRoot(new TreeNode<String>("trn:RemoveSymbolsTransformation"));
		//attributeS.addChild(new TreeNode<String>("ßhttp://www.tdg-seville.info/schema/authorsß"));
		attributeS.setRoot(new TreeNode<String>("ßhttp://www.tdg-seville.info/schema/authorsß"));
			
		
		Tree jaccard = new Tree();
		jaccard.setRoot(new TreeNode<String>("str:CosineSimilarity"));
		jaccard.addChild(attributeS);
		jaccard.addChild(attributeT);
		jaccard.addChild(new TreeNode<String>("0.9"));
		

		
		Tree jaccard2 = new Tree();
		jaccard2.setRoot(new TreeNode<String>("str:JaroSimilarity"));
		//jaccard2.addChild(new TreeNode<String>("∂http://schema.org/title∂"));
		//jaccard2.addChild(new TreeNode<String>("ßhttp://www.tdg-seville.info/schema/articleTitleß"));
		jaccard2.addChild(new TreeNode<String>("∂http://schema.org/title∂"));
		jaccard2.addChild(new TreeNode<String>("ßhttp://schema.org/titleß"));
		jaccard2.addChild(new TreeNode<String>("0.70"));
		
		
		Tree ls = new Tree();
		ls.setRoot(new TreeNode<String>("agg:Maximum"));
		//ls.addChild(jaccard);
		ls.addChild(jaccard2);
	
		

		ISpecification<Tree> specification = new LinkSpecification(ls);
		specification.setSourceRestrictions(Lists.newArrayList("http://schema.org/CoraResource"));
		specification.setTargetRestrictions(Lists.newArrayList("http://schema.org/CoraResource"));
		
		
		try {
			IEnvironment environment = Environments.parseFromFile("./experiments/environments/cora.cnf");
			
			
		/*	System.out.println("----------- Regular");
			
			SparqlTranslator translator = new SparqlTranslator();
			Tuple<String,String> queries =  translator.translate(specification);
			long startTime0 = System.nanoTime();
				
			//ILinker linker = new Linker();
			//linker.setDatasetSource(environment.getSourceDatasetFile());
			//linker.setDatasetTarget(environment.getTargetDatasetFile());
			//linker.linkDatasets(queries, "");
			
			long endTime0 = System.nanoTime();
			long duration0 = (endTime0 - startTime0);
			System.out.println("#############################>Duration(s): "+(duration0/1000000000.0));
		*/
			
			// CACHED Linker
			
			Integer kFoldSize = 10;
			String workingFolderName = "setup0/1-fold_datasets-"+Utils.getCurrentTime();
			
			StringBuffer logsDirectories = new StringBuffer();
			logsDirectories.append(FrameworkConfiguration.FRAMEWORK_WORKSPACE_DIRECTORY).append("/results/").append(environment.getName()).append("/").append(workingFolderName).append("/");
			FrameworkConfiguration.traceLog = new Logger(new StringBuffer(logsDirectories).append("traceLog.txt").toString(), 10000);		
			FrameworkConfiguration.resultsLog = new Logger(new StringBuffer(logsDirectories).append("resultsLog.txt").toString(), 10000);	
			
			//StringTranslator translator = new StringTranslator();
			SparqlTranslator translator = new SparqlTranslator();
			KFoldEvaluator evaluator = new KFoldEvaluator(environment, kFoldSize, workingFolderName, 1600);
			int i=0;
			while(i <10) {
				System.out.println("--------> Training");
				long startTime1 = System.nanoTime();
				evaluator.evaluate(translator.translate(specification));
				long endTime1 = System.nanoTime();
				long duration1 = (endTime1 - startTime1);
				System.out.println("#############################>Duration(s): "+(duration1/1000000000.0));
				
				System.out.println("--------> Validation");
				long startTime0 = System.nanoTime();
				evaluator.apply(translator.translate(specification));
				long endTime0 = System.nanoTime();
				long duration0 = (endTime0 - startTime0);
				System.out.println("Validation matrix -> "+ evaluator.getValidationMatrix()+" ::> "+evaluator.getValidationMatrix().getFMeasure());
				System.out.println("#############################>Duration(s): "+(duration0/1000000000.0));
				i++;
				break;
			}
			/*
			long startTime1 = System.nanoTime();
			ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(190);
			List<Callable<Integer>> futures = Lists.newArrayList();
			int i=0;
			while(i<20) {
				Callable<Integer> task = () -> {
					evaluator.apply(specification.toString());
					return 1;
				};
				futures.add(task);
				i++;
				break;
			}
			System.out.println("link specifications loaded ");
			EXECUTOR_SERVICE.invokeAll(futures).stream().forEach(future -> {
				try {
					System.out.println(("Specification generates: "+future.get()+" links"));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			EXECUTOR_SERVICE.shutdown();
			//
			long endTime1 = System.nanoTime();
			long duration1 = (endTime1 - startTime1);
			
			System.out.println("#############################1>Duration(s) cached: "+(duration0/1000000000.0));
		
			//  H2 EXECUTION
			/*
			ITranslator translator2 = new H2Translator();
			Tuple<String,String> queries2 = (Tuple<String, String>) translator2.translate(specification,environment);
			
			long startTime9 = System.nanoTime();
			String database = "/Users/andrea/Desktop/h2";//FrameworkConfiguration.SOURCE_H2_DATABASE;
			ILinker linker2 = new H2Linker(database);
			linker2.setDatasetSource(environment.getSourceDatasetFile());
			linker2.setDatasetTarget(environment.getTargetDatasetFile());
			
			ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(190);
			List<Callable<Set<Tuple<String,String>>>> futures = Lists.newArrayList();
			int i=0;
			while(i<20) {
				Callable<Set<Tuple<String,String>>> task = () -> {
					linker2.linkDatasets(queries2, "");
					return linker2.getInstancesLinked();
				};
				futures.add(task);
				i++;
			}
			System.out.println("link specifications loaded ");
			EXECUTOR_SERVICE.invokeAll(futures).stream().forEach(future -> {
				try {
					System.out.println(("Specification generates: "+future.get().size()+" links"));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			EXECUTOR_SERVICE.shutdown();
			long endTime9 = System.nanoTime();
			long duration9 = (endTime9 - startTime9);
			
			System.out.println("#############################>Duration(s) h2: "+(duration9/1000000000.0));
		
			
			
			
			
			
			//System.out.println("Found: "+linker.getInstancesLinked().size());
		    /*Double summ = 0.0;
		    for(Double num:times) {
		    		summ+=num;
		    }
		    System.out.println("Avg(s):"+(summ/times.size()));*/
			
			
			/*
	
			System.out.println("----------- Regular");
			long startTime0 = System.nanoTime();
			IEngine engine0 = new SparqlEngine(environment);
			String workingFolderName0 = 2+"-fold_datasets-"+Utils.getCurrentTime();
			KFoldEvaluator validator0 = new KFoldEvaluator(environment, 2, workingFolderName0);
			validator0.
			validator0.foldDatasets(true, true, false, -1);
			
			
			engine0.setEvaluator(validator0);
			ConfusionMatrix results0 = engine0.evaluate(specification);
			System.out.println("Trainig over k-folded-std: "+results0+" "+results0.getPrecision()+" "+results0.getRecall()+" "+results0.getFMeasure());
			long endTime0 = System.nanoTime();
			long duration0 = (endTime0 - startTime0);
			System.out.println("#############################>Duration 1 (std): "+(duration0/1000000));
			startTime0 = System.nanoTime();
			SparqlTranslator translator0 = new SparqlTranslator();
			validator0.apply(translator0.translate(specification));
			results0 = validator0.getValidationMatrix();
			System.out.println("Validation over k-folded-std: "+results0+" "+results0.getPrecision()+" "+results0.getRecall()+" "+results0.getFMeasure());

			endTime0 = System.nanoTime();
			duration0 = (endTime0 - startTime0);
			System.out.println("#############################>Duration 1 (std): "+(duration0/1000000));
			
			
			
			/*
			
			System.out.println("----------- Cross");
			long startTime = System.nanoTime();
			IEngine engine = new SparqlEngine(environment);
			String workingFolderName = 2+"-fold_datasets-"+Utils.getCurrentTime();
			KFoldEvaluatorInMemory validator = new KFoldEvaluatorInMemory(environment, 2, workingFolderName);
			validator.foldDatasets(true, true, false, -1);
			
			
			engine.setEvaluator(validator);
			ConfusionMatrix results = engine.evaluate(specification);
			System.out.println("Trainig over k-folded-std: "+results+" "+results.getPrecision()+" "+results.getRecall()+" "+results.getFMeasure());
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			System.out.println("#############################>Duration 2 (std): "+(duration/1000000));
		
			long startTime1 = System.nanoTime();
			SparqlTranslator translator = new SparqlTranslator();
			validator.apply(translator.translate(specification));
			results = validator.getValidationMatrix();
			System.out.println("Validation over k-folded-std: "+results+" "+results.getPrecision()+" "+results.getRecall()+" "+results.getFMeasure());
			
			long endTime1 = System.nanoTime();
			long duration1 = (endTime1 - startTime1);
			System.out.println("#############################>Duration 1 (std): "+(duration1/1000000));
			/*
			long startTime2 = System.nanoTime();
			SparqlTranslator translator = new SparqlTranslator();
			LinkerHDT linkerH = new LinkerHDT();
			linkerH.setDatasetSource("hdt-data/restaurants1-kFold_2_1.hdt");
			linkerH.setDatasetTarget("hdt-data/restaurants2-kFold_2_1.hdt");
			linkerH.linkDatasets(translator.translate(specification), "");
			
			long endTime2 = System.nanoTime();
			long duration2 = (endTime2 - startTime2);
			System.out.println("#############################>HDT 1 (std): "+(duration2/1000000));
			*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*
		long startTime3 = System.nanoTime();
		ILinker linker3 = new Linker();
		linker3.setDatasetSource(sourceTDB);
		linker3.setDatasetTarget(targetTDB);
		SparqlTranslator translator3 = new SparqlTranslator();
		//System.out.println(translator2.translate(specification));
		linker3.linkDatasets(translator3.translate(specification), "");
		long endTime3 = System.nanoTime();
		long duration3 = (endTime3 - startTime3);
		System.out.println("#############################>Duration 2 (mod): "+(duration3/1000000));
*/
		
		
		/*
		LinkageEvaluator evaluator = new LinkageEvaluator();
		String goldFile = "./experiments/gold-stds/restaurant1-restaurant2-gold.nt";
		List<String> goldLinks = evaluator.readGoldLinks(goldFile);
		String results = evaluator.getMetrics(goldLinks, "./linkage.nt");
		System.out.println(results);*/
	}
	
	
	public static StoreParams store_params_12() {
	    String xs = "{ \n" + 
	    		"  \"tdb.file_mode\" :               \"mapped\" ,\n" + 
	    		"  \"tdb.block_size\" :              2048 ,\n" + 
	    		"  \"tdb.block_read_cache_size\" :   100000000 ,\n" + 
	    		"  \"tdb.block_write_cache_size\" :  2000 ,\n" + 
	    		"  \"tdb.node2nodeid_cache_size\" :  10000000 ,\n" + 
	    		"  \"tdb.nodeid2node_cache_size\" :  50000000 ,\n" + 
	    		"  \"tdb.node_miss_cache_size\" :    100 ,\n" + 
	    		"  \"tdb.index_node2id\" :           \"node2id\" ,\n" + 
	    		"  \"tdb.index_id2node\" :           \"nodes\" ,\n" + 
	    		"  \"tdb.triple_index_primary\" :    \"SPO\" ,\n" + 
	    		"  \"tdb.triple_indexes\" :          [ \"SPO\" , \"POS\" , \"OSP\" ] ,\n" + 
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
	
	
}
