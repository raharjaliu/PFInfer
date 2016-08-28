package filter;

@Deprecated
public class GammaDist {

	private String name;
	private double slope;
	private double skewness;

	public GammaDist(String _name) {
		this.name = _name;
		this.slope = 0;
		this.skewness = 0;
	}

	public GammaDist(String _name, double _slope, double _skewness) {
		this.name = _name;
		this.slope = _slope;
		this.skewness = _skewness;
	}

	public double sample() {
		return 0.1;
	}

	public void update(double executedNum, double propSum) {
		this.slope += executedNum;
		this.skewness += propSum;
	}

	public void update(SimulationStatistics stats) {
		this.slope += stats.getExecutedNum().get(this.name);
		this.skewness += stats.getPropSum().get(this.name);
	}

}
