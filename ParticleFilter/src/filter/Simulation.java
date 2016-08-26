package filter;

import java.util.HashMap;

public class Simulation {

	private Model model;

	public Simulation(Model m) {
		model = m;
	}

	public Model getModel() {
		return model;
	}

	public SimulationStatistics runSimulation(Double runTime) {
		
		HashMap<String, String> propensitymap = model.getPropensities();
		
		SimulationStatistics statistics = new SimulationStatistics(propensitymap.keySet());

		return statistics;
	}
}
