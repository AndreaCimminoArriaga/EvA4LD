package tdg.link_discovery.tools;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.objects.TreeNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class TreeTranslator {
	

	private Map<String, Integer> treeNodeStatistics;
	private String cmpRegex = "";
	private Map<String, Tree> auxiliarSubTrees;
	

	@SuppressWarnings("unused")
	private String cmpPrefix, aggPrefix, trnsPrefix;
	private int current_mult;

	public TreeTranslator(){
		treeNodeStatistics = Maps.newHashMap();
		auxiliarSubTrees = Maps.newHashMap();
		
		cmpPrefix = SPARQLFactory.prefixJenaFunctionsStrings;
		aggPrefix = SPARQLFactory.prefixJenaFunctionsAggregations;
		trnsPrefix = SPARQLFactory.prefixJenaFunctionsTransformations;
		
		
		this.treeNodeStatistics.put(SPARQLFactory.prefixJenaFunctionsAggregations, 0);
		this.treeNodeStatistics.put(SPARQLFactory.prefixJenaFunctionsStrings, 0);
		this.treeNodeStatistics.put(SPARQLFactory.prefixJenaFunctionsTransformations, 0);
		
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(SPARQLFactory.prefixJenaFunctionsStrings).append("[a-zA-Z]+\\(∂[^∂ß,]+∂,ß[^ß∂,]+ß,\\d\\.\\d+\\)");
		this.cmpRegex = strBuff.toString();	
		
		current_mult = 0;
	}
	
	/*
	 * Tree node statistics
	 */

	public Map<String, Integer> getTreeNodeStatistics() {
		return treeNodeStatistics;
	}
	
	private void updateTreeNodeStatistics(String key){
		if(this.treeNodeStatistics.containsKey(key)){
			Integer scoring = this.treeNodeStatistics.get(key);
			scoring++;
			this.treeNodeStatistics.put(key, scoring);
		}else{
			this.treeNodeStatistics.put(key, 1);
		}
	}
	

	
	
	/*
	 *  Parsing string to a tree
	 */
	
	public Tree parseLinkSpecification(String ls){
		Tree linkTree = new Tree();
		String newLs = parseComparisons(ls);
		
		newLs = parseMult(newLs);
		linkTree = parseAgg(newLs);
		return linkTree;
	}
	
	
	/*
	 * Node Parsers
	 */
	
	private String parseComparisons(String linkSpecification){
		Pattern pattern = Pattern.compile(cmpRegex);
        Matcher matcher = pattern.matcher(linkSpecification);
        String newLs = linkSpecification;
    	int start = 0;
    	int current_cmp = 0;
    	StringBuffer str = new StringBuffer();
		while (matcher.find(start)) {
			String currentCmp = matcher.group();
			String key = str.append("∂").append(current_cmp).append("∂").toString();
			newLs = newLs.replace(currentCmp, key);
			storeLeafsComparisonsOfTree(key, currentCmp);
			updateTreeNodeStatistics(cmpPrefix);
			start = matcher.start() + 1;
			str = new StringBuffer();
			current_cmp++;
		}
		newLs = newLs.replaceAll("[^a-zA-Z:\\d\\.\\(\\)∂ß,]", "");
		return newLs;
	}
	
	
	private String parseMult(String linkSpecification){
		String patters = aggPrefix+"[a-zA-Z]+\\(∂\\d+∂,\\d\\.\\d+\\)";
		Pattern pattern = Pattern.compile(patters);
		Matcher matcher = pattern.matcher(linkSpecification);
		String newLs = linkSpecification;
		int start = 0;
		
		StringBuffer str = new StringBuffer();
    	while (matcher.find(start)) {
    		String multNode = matcher.group();
    		String key = str.append("ß").append(current_mult).append("ß").toString();
			newLs = newLs.replace(multNode, key);
			storeMultNodesOfTree(key.toString(), multNode);
			updateTreeNodeStatistics(aggPrefix);
			start = matcher.start() + 1;
			current_mult++;
			str = new StringBuffer();
		}
    	newLs = newLs.replaceAll("[^a-zA-Z:\\d\\.\\(\\)∂ß,]", "");
		return newLs;
	}
	
	private Tree parseAgg(String ls){
	
		while(ls.startsWith(aggPrefix)){
			Pattern pattern = Pattern.compile(aggPrefix+"[A-Za-z]+\\((ß\\d+ß,)*ß\\d+ß\\)");
	        Matcher matcher = pattern.matcher(ls);
	        String newLs = ls;
			int start = 0;
			
	    	StringBuffer str = new StringBuffer();
			while (matcher.find(start)) {
				String currentAgg = matcher.group();
				String key = str.append("ß").append(current_mult).append("ß").toString();
				newLs = newLs.replace(currentAgg, key);
				
				try{
					storeAggregateNodesOfTree(key, currentAgg);
				}catch(Exception e){
					e.printStackTrace();
				}
				updateTreeNodeStatistics(aggPrefix);
				start = matcher.start() + 1;
				str = new StringBuffer();
				current_mult++;
			}
			ls = newLs.replaceAll("[^a-zA-Z:\\d\\.\\(\\)∂ß,]", "");	
		}
		Tree linkTree = this.auxiliarSubTrees.get(ls);
		return linkTree;
	}
	
	
	/*
	 *  Tree node creators
	 */
	
	
	private void storeLeafsComparisonsOfTree(String key, String cmpStr){
		// Locate threshold and retreieve it, removing it from cmp node
		Pattern pattern = Pattern.compile(",\\d\\.\\d+\\)");
        Matcher matcher = pattern.matcher(cmpStr);
        String threshold = "";
        if(matcher.find())
        	threshold = matcher.group();
		cmpStr = cmpStr.replace(threshold, "");
		// TODO: In case of transformation parse them here
		//
		// Create tree, cmpNode as root and threshold as offspring
		TreeNode<String> nodeCmp = new TreeNode<String>();
		nodeCmp.setNode(cmpStr);
		TreeNode<String> nodeThreshold = new TreeNode<String>();
		nodeThreshold.setNode(threshold);
		Tree tree = new Tree();
		tree.setRoot(nodeCmp);
		tree.addChild(nodeThreshold);
		// Update operations map
	
		this.auxiliarSubTrees.put(key, tree);
	}
	
	
	private void storeMultNodesOfTree(String key, String multNodeStr){
		// Locate weight and retreieve it, removing it from mult node
		Pattern pattern = Pattern.compile(",\\d\\.\\d+\\)");
		Matcher matcher = pattern.matcher(multNodeStr);
		String weight = "";
		if(matcher.find())
			weight = matcher.group();
		multNodeStr = multNodeStr.replace(weight, "");
		// Locate cmp chil node, removing it from mult node
		pattern = Pattern.compile("∂\\d+∂");
		matcher = pattern.matcher(multNodeStr);
		String cmpKey = "";
		if(matcher.find())
			cmpKey = matcher.group();
		
		Tree cmpTree = this.auxiliarSubTrees.get(cmpKey);
		// Generate parents layer in tree
		TreeNode<String> multNode = new TreeNode<String>();
		multNode.setNode(multNodeStr.replace(cmpKey, ""));
		TreeNode<String> weightNode = new TreeNode<String>();
		weightNode.setNode(weight);
		Tree newTree = new Tree();
		newTree.setRoot(multNode);
		newTree.setChilds(Lists.newArrayList(cmpTree));
		newTree.addChild(weightNode);
	
		// Update operations map
		this.auxiliarSubTrees.put(key, newTree);
	}
	
	private void storeAggregateNodesOfTree(String key, String aggNodeStr) throws CloneNotSupportedException{
		
		List<Tree> childs = Lists.newArrayList();
		for(String childKey:aggNodeStr.replaceAll("[a-z]+:[A-Za-z]+\\(", "").split(",")){
			// Process different number of childs
			Tree child = new Tree();
			
			if(childKey.endsWith(")")){
				String specialKey = childKey.replace(")", "");
				child = this.auxiliarSubTrees.get(specialKey).clone();
				addTokenAtTheEndOfTree(child, ")");
				aggNodeStr = aggNodeStr.replace(specialKey, "");
			}else{
				child = this.auxiliarSubTrees.get(childKey).clone();
				addTokenAtTheEndOfTree(child, ",");
				aggNodeStr = aggNodeStr.replace(childKey, "");
			}
			
			if(child.getRoot().getNode()!=null)
				childs.add(child);
		}
		
		// Generate parents layer in tree
		Tree newTree = new Tree();
		TreeNode<String> root = new TreeNode<String>();	
		root.setNode(aggNodeStr.replace(",","").replace(")", ""));
		newTree.setRoot(root);
		newTree.setChilds(childs);
	
		// Update operations map
		this.auxiliarSubTrees.put(key, newTree);
	}
	
	
	
	public Map<String, Tree> getAuxiliarSubTrees() {
		return auxiliarSubTrees;
	}

	@SuppressWarnings("unchecked")
	private void addTokenAtTheEndOfTree(Tree tree, String token){
		
		if(tree!=null){
			if(tree.getChilds().isEmpty() ){
				TreeNode<String> root = (TreeNode<String>) tree.getRoot();
				String value = root.getNode()+token;
				root.setNode(value);
			}else{
				int lastChild = tree.getChilds().size()-1;
				addTokenAtTheEndOfTree(tree.getChilds().get(lastChild), token);
				
			}
		}
	}
}
