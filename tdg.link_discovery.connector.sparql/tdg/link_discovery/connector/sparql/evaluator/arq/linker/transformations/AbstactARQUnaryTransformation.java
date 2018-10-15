package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.middleware.objects.Tuple;

public abstract class AbstactARQUnaryTransformation extends FunctionBase1 implements IARQUnaryTransformation{

	protected String name;
	
	public AbstactARQUnaryTransformation(String name){
		StringBuffer str = new StringBuffer();
		str.append(SPARQLFactory.prefixJenaFunctionsTransformations).append(name);
		this.name = str.toString();
	}
	
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public NodeValue exec(NodeValue v) {
		String value = v.getString();
		StringBuilder newValue = new StringBuilder();
		String tokenization = "";
		
		if(SPARQLFactory.hasTokenization(value)){
			Tuple<String,String> trokenizationAndValue = SPARQLFactory.extractTokenization(value);
			value = trokenizationAndValue.getSecondElement(); 
			tokenization = trokenizationAndValue.getFirstElement();
		}
		
		newValue.append(tokenization).append(applyUnaryTransformation(value));
		
		NodeValue resultantNode = NodeValue.makeString(newValue.toString());
		return resultantNode;
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		AbstactARQUnaryTransformation other = (AbstactARQUnaryTransformation) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

	
	

}
