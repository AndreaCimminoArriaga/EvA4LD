package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations;

public class UpercaseTransformation extends AbstactARQUnaryTransformation{

	public UpercaseTransformation() {
		super("UpercaseTransformation");
	}
	

	@Override
	public String applyUnaryTransformation(String element) {
		return element.toUpperCase();
	}

}
