package tdg.link_discovery.middleware.objects;

public class ConfusionMatrix {

	protected Integer truePositives, trueNegatives, falsePositives, falseNegatives;

	public ConfusionMatrix(){
		super();
		truePositives = 0;
		trueNegatives = 0;
		falsePositives = 0;
		falseNegatives = 0;

	}
	
	public Integer getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(Integer truePositives) {
		this.truePositives = truePositives;
	}

	public Integer getTrueNegatives() {
		return trueNegatives;
	}

	public void setTrueNegatives(Integer trueNegatives) {
		this.trueNegatives = trueNegatives;
	}

	public Integer getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(Integer falsePositives) {
		this.falsePositives = falsePositives;
	}

	public Integer getFalseNegatives() {
		return falseNegatives;
	}

	public void setFalseNegatives(Integer falseNegatives) {
		this.falseNegatives = falseNegatives;
	}
	
	public Double getMcc(){	
		Double arg0 = (1.0*truePositives*trueNegatives) - ( falsePositives* falseNegatives);
		Double arg1 = 1.0*(truePositives+ falsePositives)*(truePositives+falseNegatives)*(trueNegatives+falsePositives)*(trueNegatives+falseNegatives);
		Double mcc = arg0/Math.sqrt(arg1);
		if(mcc.isNaN())
			mcc = 0.0;
		return mcc;
	}

	public Double getPrecision(){
		Double score =  this.truePositives*1.0 / (this.falsePositives+this.truePositives);
		if(score.isInfinite() || score.isNaN())
			score = 0.0;
		return score;
	}
	
	public Double getRecall(){
		Double score = this.truePositives*1.0 / (this.truePositives+this.falseNegatives);
		if(score.isInfinite() || score.isNaN())
			score = 0.0;
		return score;
	}
	
	public Double getFMeasure(){
		Double score = (2*getPrecision()*getRecall()) / (getPrecision()+getRecall());
		if(score.isNaN())
			score = 0.0;
		return score;
	}
	
	
	
	@Override
	public String toString() {
		return "Metrics [truePositives=" + truePositives + ", trueNegatives="
				+ trueNegatives + ", falsePositives=" + falsePositives
				+ ", falseNegatives=" + falseNegatives + "]";
	}
	
	
	
}
