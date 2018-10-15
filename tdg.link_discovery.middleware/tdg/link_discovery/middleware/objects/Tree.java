package tdg.link_discovery.middleware.objects;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.com.google.common.collect.Maps;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;

import com.google.common.collect.Lists;

public class Tree {
	
	protected TreeNode<?> root;
	protected List<Tree> childs;
	
	
	public Tree(){
		super();
		childs = Lists.newArrayList();
		root = null;
	}
	
	public TreeNode<?> getRoot() {
		return root;
	}
	
	public void setRoot(TreeNode<?> root) {
		this.root = root;
	}
	
	public List<Tree> getChilds() {
		return childs;
	}
	
	public void setChilds(List<Tree> childs) {
		this.childs = childs;
	}
	
	public void addChild(TreeNode<?> child){
		Tree tree = new Tree();
		tree.setRoot(child);
		this.childs.add(tree);
	}
	
	public void addChild(Tree child){
		this.childs.add(child);
	}


	public Integer size() {
		Integer size = 1;
		for(Tree tree:this.childs)
			size += tree.size();
		return size;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(root);
		if(!childs.isEmpty()){
			str.append("(");	
			for(Tree child:childs)
				str.append(child.toString()).append(",");
			str.replace(str.lastIndexOf(","), str.lastIndexOf(",")+1, "");
			str.append(")");
		}
		return str.toString();
	}

	public String toStructure() {
		StringBuilder str = new StringBuilder();
		str.append(root.nodeType);
		if(!childs.isEmpty()){
			str.append("(");	
			for(Tree child:childs)
				str.append(child.toStructure()).append(",");
			str.replace(str.lastIndexOf(","), str.lastIndexOf(",")+1, "");
			str.append(")");
		}
		return str.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childs == null) ? 0 : childs.hashCode());
		result = prime * result + ((root == null) ? 0 : root.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tree other = (Tree) obj;
		if (childs == null) {
			if (other.childs != null)
				return false;
		} else if (!childs.equals(other.childs))
			return false;
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equals(other.root))
			return false;
		return true;
	}	
	
	
	

	
	@Override
	@SuppressWarnings("unchecked")
	public Tree clone() throws CloneNotSupportedException {
		TreeNode<String> newRoot = (TreeNode<String>) this.root.clone();
	    
		List<Tree> newChilds = Lists.newArrayList();
	    for(Tree child:this.childs){	
	    	newChilds.add(child.clone());
	    }
	    	    
	    Tree tree = new Tree();
	    tree.setRoot(newRoot);
	    tree.setChilds(newChilds);
		return tree;
	}
	
	/*
	 * Get statistics
	 */
	public Map<String,Integer> getStatistics(){
		String structure = this.toStructure();
		Map<String,Integer> map = Maps.newHashMap();
		addEntry(SPARQLFactory.prefixJenaFunctionsAggregations, structure, map);
		addEntry(SPARQLFactory.prefixJenaFunctionsStrings, structure, map);
		addEntry(SPARQLFactory.prefixJenaFunctionsTransformations, structure, map);
		addEntry(SPARQLFactory.prefixSourceAttr, structure, map);
		addEntry(SPARQLFactory.prefixTargetAttr, structure, map);
		addEntry(SPARQLFactory.prefixThresholds, structure, map);
		addEntry(SPARQLFactory.prefixWeigts, structure, map);
		return map;
	}
	
	private void addEntry(String prefix, String structure, Map<String,Integer> map){
		Integer objectCount = StringUtils.countMatches(structure, prefix);
		if(map.containsKey(prefix)){
			Integer oldCount = map.get(prefix);
			oldCount += objectCount;
			map.put(prefix, oldCount);
		}else{
			map.put(prefix, objectCount);
		}
	}
	

	
	
	
	/*
	 * Get sub-tree
	 */
	
	private int nodeGetSubTreeCounter;
	public Tree getSubTree(String nodeStartsWith, Integer numberOfNode){
		Tree result = null;
		try{
			nodeGetSubTreeCounter = numberOfNode;
			result =  getSubTreeAuxiliarGetSubTree(this.clone(), nodeStartsWith);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(result==null)
			System.out.println("Check Tree class, method getSubTree: method is not returning the proper subtree");
		return result;
	}
	
	private Tree getSubTreeAuxiliarGetSubTree(Tree tree, String nodePattern){
		Tree treeResult = null;
		Boolean matchesInputNodeType = tree.getRoot().getNodeType().startsWith(nodePattern);	
		if(nodeGetSubTreeCounter == 0 && matchesInputNodeType){
			treeResult = tree;
			
		}else{
			if(matchesInputNodeType)
				nodeGetSubTreeCounter--;
			
			for(Tree child:tree.getChilds()){
				Tree resultant = getSubTreeAuxiliarGetSubTree(child, nodePattern);
				if(resultant != null){
					treeResult = resultant;
					break;
				}
			}	
		}
		return treeResult;
	}
	
	
	
	/*
	 * Replace sub-tree
	 */
	private int nodeCounter;
	public Tree replaceSubTreeFromNode(String nodeStartsWith, Integer numberOfNode, Tree subTree ){
		Tree newTree = null;
		try{
			nodeCounter = numberOfNode;
			newTree = this.clone();
			getSubTreeAuxiliarReplaceSubTree(newTree, subTree.clone(), nodeStartsWith);
		}catch(Exception e){
			e.printStackTrace();
		}
		return newTree;
	}
	
	@SuppressWarnings("unchecked")
	private void getSubTreeAuxiliarReplaceSubTree(Tree tree, Tree subTree, String nodePattern){
		
			Boolean matchesInputNodeType = tree.getRoot().getNodeType().startsWith(nodePattern);	
			if(nodeCounter == 0 && matchesInputNodeType){
				TreeNode<String> root = (TreeNode<String>) subTree.getRoot();
				tree.setRoot(root);
				tree.setChilds(subTree.getChilds());
				// Since the tree is explored relyin on bfs, we need to decrement the counter 
				// to avoid parallel branch exploration match also this node
				nodeCounter--; 
		
			}else{
				if(matchesInputNodeType)
					nodeCounter--;
				
				for(Tree child:tree.getChilds()){
					getSubTreeAuxiliarReplaceSubTree(child, subTree, nodePattern);
				}
			}
		
	}

	
	
}
