package filter;

import java.util.HashMap;


public class Simulation {

	private Model model;

	public Simulation(Model m) {
		this.model = m;
	}

	public Model getModel() {
		return this.model;
	}

	public SimulationStatistics runSimulation(Double runTime) {

		SimulationStatistics statistics = new SimulationStatistics(this.model
				.getPropensities().keySet());

		int reactionumber = this.model.getPropensities().size();
		Double timestep = 0.0;

		String[] reaction = new String[reactionumber];
		Double[] propensity = new Double[reactionumber];

		HashMap<String, Integer> propMap = new HashMap<String, Integer>();
		HashMap<String, HashMap<String, String>> dependency = this.model.getDepencyMap();

		int counter = 0;
		for (String name : this.model.getPropensities().keySet()) {
			reaction[counter] = name;
			propensity[counter] = this.model.getPropensity(name);
			propMap.put(name, counter);
			counter++;
		}

		while (timestep < runTime) {

			Double combinedPropensities = 0.0;
			for (int i = 0; i < reactionumber; i++) {
				combinedPropensities += propensity[i];
			}

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
					break;
				}
			}

			this.model.executeReaction(chosenReaction, step);
			statistics.updateStatistic(chosenReaction,
					this.model.getPropensity(chosenReaction), step);
			
			
			
			HashMap<String, String> update = dependency.get(chosenReaction);
			
			for (String propname : update.keySet()){
				int i = propMap.get(propname);
				propensity[i] = this.model.getPropensity(propname);
				
			}

			timestep += step;
		}

		return statistics;
	}
}
