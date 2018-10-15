package tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates;

import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;

public abstract class AbstractARQAggregate extends FunctionBase implements IARQAggregate{
	
	protected final String name;
	
	public AbstractARQAggregate(String name){
		StringBuffer str = new StringBuffer();
		str.append(SPARQLFactory.prefixJenaFunctionsAggregations).append(name);
		this.name = str.toString();
	}
	
	@Override
	public Double applyAggregation(List<Double> values) {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
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
		Max other = (Max) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}



	@Override
	public NodeValue exec(List<NodeValue> args) {
		List<Double> scores = Lists.newArrayList();
		for(NodeValue node:args){
			Double value = node.getDouble();
			scores.add(value);
		}
		Double score = applyAggregation(scores);
		
		NodeValue resultantNode = NodeValue.makeDouble(score);
		return resultantNode;
	}

	@Override
	public void checkBuild(String uri, ExprList args) {
		// TODO Auto-generated method stub
		
	}

	
}
