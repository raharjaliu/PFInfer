package filter;

import java.util.HashMap;
import java.util.Set;

public class SimulationStatistics {

	private HashMap<String, Double> executedNum = new HashMap<String, Double>();
	private HashMap<String, Double> propSum = new HashMap<String, Double>();

	public SimulationStatistics(Set<String> names) {
		for (String name : names) {
			executedNum.put(name, 0.0);
			propSum.put(name, 0.0);
		}
	}

	public HashMap<String, Double> getExecutedNum() {
		return executedNum;
	}

	public HashMap<String, Double> getPropSum() {
		return propSum;
	}
	
	//TODO: check if executedNum need to be updated by 1 or by time
	public void updateStatistic (String reaction, Double propensity, Double time){
		Double temp = executedNum.get(reaction) + 1.0;
		executedNum.put(reaction, temp);
		temp = propSum.get(reaction) + (time*propensity);
		propSum.put(reaction, temp);		
	}

}
