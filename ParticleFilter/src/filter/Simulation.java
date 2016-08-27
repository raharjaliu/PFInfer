package filter;

import java.util.HashMap;

public class Simulation {

	private Model model;

	private String[] reaction;
	private Double[] propensity;
	private HashMap<String, Integer> propMap = new HashMap<String, Integer>();

	public Simulation(Model m) {
		model = m;
		int size = model.getPropensities().size();

		reaction = new String[size];
		propensity = new Double[size];

		int counter = 0;
		for (String name : model.getPropensities().keySet()) {
			reaction[counter] = name;
			propensity[counter] = model.getPropensity(name);
			propMap.put(name, counter);
			counter++;
		}

	}

	public Model getModel() {
		return model;
	}

	public SimulationStatistics runSimulation(Double runTime) {

		SimulationStatistics statistics = new SimulationStatistics(model
				.getPropensities().keySet());

		int reactionumber = model.getPropensities().size();
		Double timestep = 0.0;

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

			model.executeReaction(chosenReaction, step);
			statistics.updateStatistic(chosenReaction, model.getPropensity(chosenReaction),
					step);

			for (int i = 0; i < reactionumber; i++) {
				propensity[i] = model.getPropensity(reaction[i]);
			}

			timestep += step;
		}

		System.out.println(statistics.getExecutedNum().get("ProduceGata1"));
		System.out.println(statistics.getExecutedNum().get("ProducePu1"));
		System.out.println(statistics.getExecutedNum().get("DegradeGata1"));
		System.out.println(statistics.getExecutedNum().get("DegradePu1"));

		return statistics;
	}
}
