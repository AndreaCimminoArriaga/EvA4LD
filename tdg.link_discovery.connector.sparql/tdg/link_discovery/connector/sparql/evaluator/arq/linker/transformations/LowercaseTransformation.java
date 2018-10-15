package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations;

public class LowercaseTransformation extends AbstactARQUnaryTransformation{

	public LowercaseTransformation() {
		super("LowercaseTransformation");
	}
	
	
	@Override
	public String applyUnaryTransformation(String element) {
		return element.toLowerCase();
	}

}
