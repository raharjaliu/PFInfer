package filter;

import java.util.HashMap;
import java.util.Set;

/**
 * 
 * Class used by an instance of {@link Simulation} to store information about
 * each reactions execution number and propensity sum
 * 
 * @author Rene Schoeffel
 */

public class SimulationStatistics {

	private HashMap<String, Double> executedNum = new HashMap<String, Double>();
	private HashMap<String, Double> propSum = new HashMap<String, Double>();

	/**
	 * Initializes {@link SimulationStatistics}
	 * 
	 * @param names
	 *            of all reaction whose execution number and propensitiy sum is
	 *            to be tracked
	 */

	public SimulationStatistics(Set<String> names) {
		for (String name : names) {
			this.executedNum.put(name, 0.0);
			this.propSum.put(name, 0.0);
		}
	}

	/**
	 * Return this Simulationsatistics' executedNum HashMap
	 * 
	 * @return HashMap<String, Double> mapping reaction names to their execution number
	 */
	
	public HashMap<String, Double> getExecutedNum() {
		return this.executedNum;
	}

	/**
	 * Return this Simulationsatistics' propSum HashMap
	 * 
	 * @return HashMap<String, Double> mapping reaction names to their propensity sum
	 */
	
	public HashMap<String, Double> getPropSum() {
		return this.propSum;
	}

	/**
	 * Return this Simulationsatistics' propSum HashMap
	 * 
	 * @param reaction name of executed reaction
	 * @param propensity of executed reaction
	 * @param time of executed reaction
	 */
	
	
	// TODO: check if executedNum need to be updated by 1 or by time
	public void updateStatistic(String reaction, Double propensity, Double time) {
		Double temp = this.executedNum.get(reaction) + 1.0;
		this.executedNum.put(reaction, temp);
		temp = this.propSum.get(reaction) + (time * propensity);
		this.propSum.put(reaction, temp);
	}

}
