package filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.math3.distribution.GammaDistribution;

/**
 * 
 * Class representing mathematical definition of a Particle. This class contains
 * instance of {@link Simulation} and the {@link GammaDistribution}'s that are
 * associated with the tunables within {@link Model}
 * 
 * @author Pandu Raharja-Liu
 */
public class Particle {

	private Simulation simulation;
	private Map<String, GammaDistribution> gammaDistribs;

	/**
	 * Initializes {@link Particle}
	 * 
	 * @param _simulation
	 *            an instance of {@link Simulation} that is associated with this
	 *            Particle
	 */
	public Particle(Simulation _simulation) {
		this.simulation = _simulation;
		this.gammaDistribs = new HashMap<String, GammaDistribution>();

		HashMap<String, Double> tunables = _simulation.getModel().getTunable();
		for (String tunable : tunables.keySet()) {
			double shape = 1;
			double scale = tunables.get(tunable);
			this.gammaDistribs
					.put(tunable, new GammaDistribution(shape, scale));
		}
	}

	/**
	 * Return concentration of a given species. The concentration is contained
	 * within the {@link Model} field that is contained within this class'
	 * {@link Simulation} instance
	 * 
	 * @param speciesname
	 * @return concentration of the species
	 */
	public Double getConcentration(String speciesname) {
		return this.simulation.getModel().getSpecies(speciesname);
	}

	/**
	 * Deep copy this instance of {@link Particle}
	 * 
	 * @return another instance of {@link Particle} with each fields within it
	 *         being isomorph to the original instance
	 */
	public Particle deepCopy() {

		Model mod = this.simulation.getModel().deepCopy();
		Simulation sim = new Simulation(mod);
		return new Particle(sim);

	}

	/**
	 * Given the statistics from previous simulation run, update Gamma
	 * distribution that is associated with each Tunable within the
	 * {@link Model}, which is contained in this class' {@link Simulation}
	 * instance
	 * 
	 * @param stats
	 */
	public void gammaUpdateAndSample(SimulationStatistics stats) {

		Model model = this.simulation.getModel();
		HashMap<String, Double> tunable = model.getTunable();
		HashMap<String, HashSet<String>> tunableReactionMap = model
				.getTunableReactionMap();

		for (String thisTunable : tunable.keySet()) {

			GammaDistribution thisGamma = this.gammaDistribs.get(thisTunable);
			double shape = thisGamma.getShape();
			double scale = thisGamma.getScale();

			HashSet<String> reactions = tunableReactionMap.get(thisTunable);

			// gamma update
			double newShape = shape;
			double newScale = 1 / scale;
			for (String reaction : reactions) {
				newShape += stats.getExecutedNum().get(reaction);
				newScale += stats.getPropSum().get(reaction);
			}
			newScale = 1 / newScale;
			thisGamma = new GammaDistribution(newShape, newScale);
			this.gammaDistribs.put(thisTunable, thisGamma);

			// double newShape = shape +
			// stats.getExecutedNum().get(model.thisTunable));
			// double newScale = 1/((1/scale) +
			// stats.getPropSum().get(model.thisTunable)); // rate := 1 /
			// scale!!

			// gamma sample
			double newTunable = thisGamma.sample();
			this.simulation.getModel().setTunable(thisTunable, newTunable);
		}

	}

	/**
	 * Run the simulation. This method will call
	 * {@link Simulation#runSimulation(Double)} and return its
	 * {@link SimulationStatistics}.
	 * 
	 * @param t
	 *            total simulation run time
	 * @return {@link SimulationStatistics} produced by
	 *         {@link Simulation#runSimulation(Double)}
	 */
	public SimulationStatistics runSimulation(double t) {

		SimulationStatistics stats = this.simulation.runSimulation(t);
		gammaUpdateAndSample(stats);

		return stats;
	}

}
