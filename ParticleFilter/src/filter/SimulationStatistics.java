package filter;

import java.util.HashMap;
import java.util.Set;

public class SimulationStatistics {

	private HashMap<String, Double> executedNum = new HashMap<String, Double>();
	private HashMap<String, Double> propSum = new HashMap<String, Double>();

	public SimulationStatistics(Set<String> names) {
		for (String name : names) {
			this.executedNum.put(name, 0.0);
			this.propSum.put(name, 0.0);
		}
	}

	public HashMap<String, Double> getExecutedNum() {
		return this.executedNum;
	}

	public HashMap<String, Double> getPropSum() {
		return this.propSum;
	}
	
	//TODO: check if executedNum need to be updated by 1 or by time
	public void updateStatistic (String reaction, Double propensity, Double time){
		Double temp = this.executedNum.get(reaction) + 1.0;
		this.executedNum.put(reaction, temp);
		temp = this.propSum.get(reaction) + (time*propensity);
		this.propSum.put(reaction, temp);		
	}

}
