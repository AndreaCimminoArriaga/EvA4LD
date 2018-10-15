package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations;

public class RemoveSymbolsTransformation extends AbstactARQUnaryTransformation{

	public RemoveSymbolsTransformation() {
		super("RemoveSymbolsTransformation");
	}
	
	
	@Override
	public String applyUnaryTransformation(String element) {
		return element.replaceAll("[^a-zA-Z0-9\\s]", "");
	}

}
