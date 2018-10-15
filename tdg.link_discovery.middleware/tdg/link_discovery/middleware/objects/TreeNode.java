package tdg.link_discovery.middleware.objects;

public class TreeNode<T> {
	
	protected String nodeType;
	protected String nodeClass;
	protected T node;
	
	public TreeNode(){
		super();
		nodeType = "";
	}
	
	public TreeNode(T object){
		super();
		node = object;
		nodeType = "";
	}
	
	public String getNodeClass() {
		return nodeClass;
	}
	public void setNodeClass(String nodeClass) {
		this.nodeClass = nodeType;
	}
	public T getNode() {
		return node;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	public void setNode(T node) {
		this.node = node;
		this.nodeClass = node.getClass().getName();
		
	}
	
	@Override
	public String toString() {
		return node.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result
				+ ((nodeClass == null) ? 0 : nodeClass.hashCode());
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeNode<T> other = (TreeNode<T>) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (nodeClass == null) {
			if (other.nodeClass != null)
				return false;
		} else if (!nodeClass.equals(other.nodeClass))
			return false;
		return true;
	}
	
	
	@Override
	public TreeNode<T> clone() throws CloneNotSupportedException {
		TreeNode<T> newNode = new TreeNode<T>();
		T newObject = this.node;
		String newNodeType = this.nodeType;
		newNode.setNode(newObject);
		newNode.setNodeType(newNodeType);
		return newNode;
	}

	
}
