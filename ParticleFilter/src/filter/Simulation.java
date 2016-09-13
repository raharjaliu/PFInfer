package filter;

import java.util.HashMap;

/**
 * 
 * Class performing the Gillespie Stochastic Simulation Algorithm (SSA) for an
 * instance of {@link Model}.
 * 
 * @author Rene Schoeffel
 */

public class Simulation {

	private Model model;

	/**
	 * Initializes {@link Particle}
	 * 
	 * @param _simulation
	 *            an instance of {@link Model} that is associated with this
	 *            Simulation
	 */

	public Simulation(Model m) {
		this.model = m;
	}

	/**
	 * Return the instance of {@link Model}, which is associated with this
	 * Simulation
	 * 
	 * @return the instance of {@link Model}
	 */

	public Model getModel() {
		return this.model;
	}

	/**
	 * Perform SSA for the associated instance of {@link Model} for a given
	 * time. Return SimulationStatistics used by an instance of {@link Particle}
	 * for tunable updates
	 * 
	 * @param runTime
	 *            for which Simulation is performed
	 * @return SimulationStatistics tracking execution number and propensity sum
	 *         for all reaction
	 */

	public SimulationStatistics runSimulation(Double runTime) {

		SimulationStatistics statistics = new SimulationStatistics(this.model
				.getPropensities().keySet());

		int reactionumber = this.model.getPropensities().size();
		Double timestep = 0.0;

		String[] reaction = new String[reactionumber];
		Double[] propensity = new Double[reactionumber];

		HashMap<String, Integer> propMap = new HashMap<String, Integer>();
		HashMap<String, HashMap<String, String>> dependency = this.model
				.getDepencyMap();

		int counter = 0;
		for (String name : this.model.getPropensities().keySet()) {
			reaction[counter] = name;
			propensity[counter] = this.model.getPropensity(name);
			propMap.put(name, counter);
			counter++;
		}

		if (Main.verbose) {
			System.out.println("Starting simulation. Remaining time: "
					+ runTime);
		}

		int runCount = 0;

		while (timestep < runTime) {

			Double combinedPropensities = 0.0;
			for (int i = 0; i < reactionumber; i++) {
				combinedPropensities += propensity[i];
			}

			// System.out.println(combinedPropensities);

			Double rand1 = Math.random();
			Double rand2 = Math.random();

			Double step = -((1.0 / combinedPropensities) * Math.log(rand1));
			Double propCutoff = rand2 * combinedPropensities;

			String chosenReaction = "";
			Double currentReaction = 0.0;

			for (int i = 0; i < reactionumber; i++) {
				currentReaction += propensity[i];
				if (propCutoff < currentReaction) {
					chosenReaction = reaction[i];
					// System.out.println("chose "+chosenReaction + "!");
					break;
				}
			}

			this.model.executeReaction(chosenReaction, step);
			statistics.updateStatistic(chosenReaction,
					this.model.getPropensity(chosenReaction), step);

			HashMap<String, String> update = dependency.get(chosenReaction);

			for (String propname : update.keySet()) {
				int i = propMap.get(propname);
				propensity[i] = this.model.getPropensity(propname);

			}

			timestep += step;

			if (Main.verbose) {
				if (++runCount % 100000 == 0) {
					System.out.println("Run count: " + runCount
							+ ". Time remaining: " + (runTime - timestep));
				}
			}
		}
		return statistics;
	}
}
