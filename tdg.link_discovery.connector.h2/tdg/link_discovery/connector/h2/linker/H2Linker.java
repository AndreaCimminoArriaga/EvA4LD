package tdg.link_discovery.connector.h2.linker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Model;
import org.h2.jdbc.JdbcSQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.learner.executor.LinkSpecificationExecutor;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.StreamUtils;

public class H2Linker implements ILinker {

	private String sourceTable;
	private String targetTable;
	private String sourceDatabase;
	private String targetDatabase;
	private Boolean oneDatabase = false;
	private CopyOnWriteArraySet<Tuple<String, String>> instancesLinkedSet;
	private Set<Tuple<String,String>> sampleInstances;
	private String outputFile;
	
	// H2 parallelism
	//private static final HikariDataSource HIKARI_POOL_CONNECTOR = createInitHikari();
	//private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(190);
	private HikariDataSource HIKARI_POOL_CONNECTOR;
	private ExecutorService EXECUTOR_SERVICE;
	
	public H2Linker(String sourceDatabase, String targetDatabase) {
		this.sourceTable = "";
		this.targetTable = "";
		this.outputFile = "";
		this.sourceDatabase = sourceDatabase;
		this.targetDatabase = targetDatabase;
		oneDatabase = sourceDatabase.equals(targetDatabase);
		sampleInstances = new CopyOnWriteArraySet<Tuple<String,String>>();
		HIKARI_POOL_CONNECTOR = createInitHikariMysql();
		EXECUTOR_SERVICE = Executors.newFixedThreadPool(190);
	}
	
	public H2Linker(String database) {
		this.sourceTable = "";
		this.targetTable = "";
		this.outputFile = "";
		this.sourceDatabase = database;
		this.targetDatabase = database;
		oneDatabase = true;
		sampleInstances = new CopyOnWriteArraySet<Tuple<String,String>>();
		HIKARI_POOL_CONNECTOR = createInitHikariMysql();
		EXECUTOR_SERVICE = Executors.newFixedThreadPool(190);
	}
	
	
	/*
	 * Link whole datasets
	 */

	@Override
	public void linkDatasets(Tuple<String, String> queries, String outputFile) {
		resgisterFunctions("CREATE ALIAS IF NOT EXISTS JaroSimilarity FOR \"tdg.link_discovery.connector.h2.procedures.Procedures.jaroSimilarity\"");
		resgisterFunctions("CREATE ALIAS IF NOT EXISTS CosineSimilarity FOR \"tdg.link_discovery.connector.h2.procedures.Procedures.cosineSimilarity\"");
		resgisterFunctions("CREATE ALIAS IF NOT EXISTS Maximum FOR \"tdg.link_discovery.connector.h2.procedures.Procedures.max\"");
		this.outputFile = outputFile;
		instancesLinkedSet = new CopyOnWriteArraySet<Tuple<String,String>>();
		
		if(nonEmptyQueries(queries)){
			link(null, null, queries);
		}
		
		// TODO: saveResults();
		System.out.println("Found " + instancesLinkedSet.size()+ " between datasets");
	}
	
	
	

	private void resgisterFunctions(String function) {
		try {
			
			Connection connection = HIKARI_POOL_CONNECTOR.getConnection();
			connection.setAutoCommit(true);
			PreparedStatement result  = connection.prepareStatement(function);
			result.setFetchSize(100);
			result.executeUpdate();
	
			
			result.close();
			connection.close();	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
	/*
	 * Link only sample instances
	 */

	@Override
	public void linkInstances(Tuple<String,String> queries){
		outputFile = "";

		instancesLinkedSet = new CopyOnWriteArraySet<Tuple<String,String>>();
		if(nonEmptyQueries(queries) && !sampleInstances.isEmpty()){
			sampleInstances.stream().parallel().forEach(pair-> linkPairOfInstances(pair,queries));
		}
	}
	
	private boolean linkPairOfInstances(Tuple<String,String> instancesToLink, Tuple<String,String> queries){
		String sourceInstance = instancesToLink.getFirstElement();
		String targetInstance = instancesToLink.getSecondElement();
		link(sourceInstance, targetInstance, queries);
		return true;
	}
	
	
	/*
	 * Query applicator to database
	 */
	
	private void link(String sourceInstance, String targetInstance, Tuple<String, String> queries) {
		// Adapt queries to sample instances
		Tuple<String,String> newQueries = queries;
		if(sourceInstance!=null && targetInstance!=null) {
			// if sourceInstance and targetInstance are not null, add the clauses:
			//		"WHERE source.id = sourceInstance" to queries.getFirstElement()
			//		"WHERE target.id = targetInstance" to queries.getSecondElement()
		}
		executeSourceQuery(newQueries.getFirstElement(), newQueries.getSecondElement());
	}
	
	/* 
	 * InvokeAll callables linking mode
	*/
	private void executeSourceQuery(String sourceQuery, String targetQuery) {
		try{
			Connection connection = HIKARI_POOL_CONNECTOR.getConnection();
			
			connection.setAutoCommit(false);
			PreparedStatement result  = connection.prepareStatement(sourceQuery);
			result.setMaxRows(120);
			//result.setFetchSize(10000);
			ResultSet rs =  result.executeQuery();
			List<Callable<Set<Tuple<String,String>>>> futures = Lists.newArrayList();
			
			while (rs.next()){
				// retrieve and inject source id into target query
				String id = rs.getString("idSource");
				StringBuffer newQueryTarget = new StringBuffer(targetQuery);
				newQueryTarget.append("'").append(id).append("'");
				// Parallelize nested query	
				Callable<Set<Tuple<String,String>>> task = () -> {
				    String threadName = Thread.currentThread().getName();
				    
					System.out.println(">"+threadName);
					return hadnleTargetDataset(id, newQueryTarget.toString());
				};
				futures.add(task);
				// ------
			}
			// closing database connections
			rs.close();
			result.close();
			connection.close();	
			// Executing tasks
			System.out.println("Futures loaded ");
			EXECUTOR_SERVICE.invokeAll(futures).stream().forEach(future -> {
				try {
					this.instancesLinkedSet.addAll(future.get());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			
			
		}  catch(JdbcSQLException e) {
			System.out.println("Non blocking error, current query: "+sourceQuery+" could not be executed");
			e.printStackTrace();
		} catch( SQLException e) {
			   e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		EXECUTOR_SERVICE.shutdown();
	}
	
	/* 
	 * Submitting futures into list linking mode
	*/
	/*private static int i = 0;
	private void executeSourceQuery(String sourceQuery, String targetQuery) {
		try{
			Connection connection = HIKARI_POOL_CONNECTOR.getConnection();
			connection.setAutoCommit(false);
			PreparedStatement result  = connection.prepareStatement(sourceQuery);
			result.setMaxRows(100);
			//result.setFetchSize(10000);
			ResultSet rs =  result.executeQuery();
			
			while (rs.next()){
				// retrieve and inject source id into target query
				String id = rs.getString("idSource");
				StringBuffer newQueryTarget = new StringBuffer(targetQuery);
				newQueryTarget.append("'").append(id).append("'");
				// Parallelize nested query	
				Callable<Set<Tuple<String,String>>> task = () -> {
				    String threadName = Thread.currentThread().getName();
				    i++;
					System.out.println(">"+threadName+" ====> "+i);
					return hadnleTargetDataset(id, newQueryTarget.toString());
				};
				EXECUTOR_SERVICE.submit(task);
				// ------
			}
			System.out.println("Futures loaded ");
			 // This will make the executor accept no new threads
            // and finish all existing threads in the queue
			EXECUTOR_SERVICE.shutdown();
            // Block application until it finish
            while (!EXECUTOR_SERVICE.isTerminated()) {
            }
            // Retrieve results
			futures.parallelStream().forEach(future -> {
				try {
					this.instancesLinkedSet.addAll(future.get());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			
			// closing everything
			rs.close();
			result.close();
			connection.close();	
		}  catch(JdbcSQLException e) {
			System.out.println("Non blocking error, current query: "+sourceQuery+" could not be executed");
			e.printStackTrace();
		} catch( SQLException e) {
			   e.printStackTrace();
		} 
		EXECUTOR_SERVICE.shutdown();
	}*/
	

	
	/*	
	 * Iterative linking mode 	-----------------------
	 /* 
	 /* private void executeSourceQuery(String sourceQuery, String targetQuery) {
		try{
			Connection connection = HIKARI_POOL_CONNECTOR.getConnection();
			connection.setAutoCommit(false);
			PreparedStatement result  = connection.prepareStatement(sourceQuery);
			result.setFetchSize(10000);
			ResultSet rs =  result.executeQuery();
			int i= 0;
			while (rs.next()){
				i++;
				System.out.println(i);
				// retrieve and inject source id into target query
				String id = rs.getString("idSource");
				StringBuffer newQueryTarget = new StringBuffer(targetQuery);
				newQueryTarget.append("'").append(id).append("'");
				// Parallelize nested query	
				try {
					EXECUTOR_SERVICE.submit(() -> {
					    String threadName = Thread.currentThread().getName();
					    hadnleTargetDataset(id, newQueryTarget.toString());
					}).get();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
			// closing everything
			rs.close();
			result.close();
			connection.close();	
		}  catch(JdbcSQLException e) {
			System.out.println("Non blocking error, current query: "+sourceQuery+" could not be executed");
			e.printStackTrace();
		} catch( SQLException e) {
			   e.printStackTrace();
		}
		EXECUTOR_SERVICE.shutdown();
	}*/
	
	
	
	private Set<Tuple<String,String>> hadnleTargetDataset(String sourceId, String targetQuery){
		Set<Tuple<String,String>> instancesLinked = Sets.newConcurrentHashSet();
		try{
			Connection connection = HIKARI_POOL_CONNECTOR.getConnection();
			connection.setAutoCommit(false);
			PreparedStatement result  = connection.prepareStatement(targetQuery);
			//result.setFetchSize(10000);
			result.setMaxRows(120);
			ResultSet rs =  result.executeQuery();
			while (rs.next()){
				String targetId = rs.getString("idTarget");
				instancesLinked.add(new Tuple<String,String>(sourceId, targetId));
			}
			// closing everything
			rs.close();
			result.close();
			connection.close();	
		}  catch(JdbcSQLException e) {
			System.out.println("Non blocking error, current query: "+targetQuery+" could not be executed");
			e.printStackTrace();
		} catch( SQLException e) {
			   e.printStackTrace();
		}
		return instancesLinked;
		
	}


	


	
	/*
	 * Check input queries correctness for 'linkInstances' and 'linkDatasets'
	 */
	
	private Boolean nonEmptyQueries(Tuple<String,String> queries){
		return !queries.getFirstElement().isEmpty() && !queries.getSecondElement().isEmpty();
	}
	
	/*
	 * Getters & Setters
	 */

	@Override
	public Set<Tuple<String, String>> getInstancesLinked() {
		return instancesLinkedSet;
	}
	
	@Override
	public void setDatasetSource(String datasetSource) {
		this.sourceTable = fixDatasetsName(datasetSource);
	}

	@Override
	public void setDatasetTarget(String datasetTarget) {
		this.targetTable = fixDatasetsName(datasetTarget);
	}
	
	private String fixDatasetsName(String datasetName) {
		String newDatasetName = datasetName;
		if(datasetName.contains("/"))
			newDatasetName = datasetName.substring(datasetName.lastIndexOf("/")+1, datasetName.length());
		return newDatasetName;
	}
	
	@Override
	public void setInstances(Set<Tuple<String, String>> instances) {
		sampleInstances = instances;
	}

	@Override
	@Deprecated
	public void setDatasetSource(Model datasetSource) {
		// empty
	}

	@Override
	@Deprecated
	public void setDatasetTarget(Model datasetTarget) {
		// empty
	}

	/*
	 * Hikary initialization
	 */
	private static HikariDataSource createInitHikari() {
		HikariConfig config = new HikariConfig();
		config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
		config.setConnectionTestQuery("VALUES 1");
		config.addDataSourceProperty("URL", "jdbc:h2:file:/Users/andrea/Desktop/h2;MULTI_THREADED=1;CACHE_SIZE=4096;");
		HikariDataSource ds = new HikariDataSource(config);
		ds.setMaximumPoolSize(200);
		ds.setReadOnly(false);
		ds.setConnectionTimeout(3600000);
		return ds;
	}
	
	private static HikariDataSource createInitHikariMysql() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:mysql://localhost:8889/test");
		config.setUsername("root");
		config.setPassword("root");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "4096");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "8192");
		
		HikariDataSource ds = new HikariDataSource(config);
		ds.setMaximumPoolSize(200);
		
		return ds;
	}
	
	
}
