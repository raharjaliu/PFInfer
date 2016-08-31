package filter;

import java.util.HashMap;
import java.util.HashSet;
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
	
	public Double getConcentration(String speciesname){
		return this.simulation.getModel().getSpecies(speciesname);
	}
	
	public Particle deepCopy() {
		
		Model mod = this.simulation.getModel().deepCopy();
		Simulation sim = new Simulation(mod);
		return new Particle(sim);
		
	}
	
	public void gammaUpdateAndSample(SimulationStatistics stats) {
		
		Model model = this.simulation.getModel();
		HashMap<String, Double> tunable = model.getTunable();
		HashMap<String, HashSet<String>> tunableReactionMap = model.getTunableReactionMap();
		
		for (String thisTunable : tunable.keySet()) {
			
			GammaDistribution thisGamma = this.gammaDistribs.get(thisTunable);
			double shape = thisGamma.getShape();
			double scale = thisGamma.getScale();
			
			HashSet<String> reactions = tunableReactionMap.get(thisTunable);
			
			// gamma update
			double newShape = shape;
			double newScale = 1/scale;
			for (String reaction : reactions) {
				newShape += stats.getExecutedNum().get(reaction);
				newScale += stats.getPropSum().get(reaction);
			}
			newScale = 1/newScale;
			thisGamma = new GammaDistribution(newShape, newScale);
			this.gammaDistribs.put(thisTunable, thisGamma);
			
//			double newShape = shape + stats.getExecutedNum().get(model.thisTunable));
//			double newScale = 1/((1/scale) + stats.getPropSum().get(model.thisTunable)); // rate := 1 / scale!!
			
			// gamma sample
			double newTunable = thisGamma.sample();
			this.simulation.getModel().setTunable(thisTunable, newTunable);
		}
		
	}
	
	public SimulationStatistics runSimulation(double t) {
		
		SimulationStatistics stats = this.simulation.runSimulation(t);
		gammaUpdateAndSample(stats);
		
		return stats;
	}

}
