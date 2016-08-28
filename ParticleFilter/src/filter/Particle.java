package filter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.distribution.GammaDistribution;

public class Particle {
	
	private Simulation simulation;
	private Map<String, GammaDistribution> gammaDistribs;
	
	public Particle(Simulation _simulation) {
		this.simulation = _simulation;
		this.gammaDistribs = new HashMap<String, GammaDistribution>();
		
		HashMap<String, Double> tunables = _simulation.getModel().getTunable();
		for(String tunable : tunables.keySet()) {
			double shape = 1;
			double scale = tunables.get(tunable);
			this.gammaDistribs.put(tunable, new GammaDistribution(shape, scale));
		}
	}
	
	public Particle deepCopy() {
		
		Model mod = this.simulation.getModel().deepCopy();
		Simulation sim = new Simulation(mod);
		return new Particle(sim);
		
	}
	
//	public Simulation getSimulation() {
//		return this.simulation;
//	}
	
	public void gammaUpdateAndSample(SimulationStatistics stats) {
		
		Model model = this.simulation.getModel();
		HashMap<String, Double> tunable = model.getTunable();
		
		for (String thisTunable : tunable.keySet()) {
			GammaDistribution thisGamma = this.gammaDistribs.get(thisTunable);
			double shape = thisGamma.getShape();
			double scale = thisGamma.getScale();
			double newShape = shape + stats.getExecutedNum().get(thisTunable);
			double newScale = 1/((1/scale) + stats.getPropSum().get(thisTunable)); // rate := 1 / scale!!
			thisGamma = new GammaDistribution(newShape, newScale);
			double newTunable = thisGamma.sample();
			this.simulation.getModel().setTunable(thisTunable, newTunable);
		}
		
	}
	
	public void runSimulation(double t) {
		
		SimulationStatistics stats = this.simulation.runSimulation(t);
		gammaUpdateAndSample(stats);
	}

}
