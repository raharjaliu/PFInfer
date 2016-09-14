package filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.math3.distribution.GammaDistribution;

/**
 * 
 * Class representing mathematical definition of a Particle. This class contains
 * instance of {@link Simulation} and the {@link GammaDistribution}'s that are
 * associated with the tunables within {@link Model}
 * 
 * @author Pandu Raharja-Liu
 */
public class Particle implements Runnable {

	private Simulation simulation;
	private Map<String, GammaDistribution> gammaDistribs;
	private double nextSimulationTime;
	private ReentrantLock lock;

	/**
	 * Initializes {@link Particle}
	 * 
	 * @param _simulation
	 *            an instance of {@link Simulation} that is associated with this
	 *            Particle
	 * @param lock
	 */
	public Particle(Simulation _simulation, ReentrantLock _lock) {
		this.simulation = _simulation;
		this.lock = _lock;
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
	 * Return model of the particle. The {@link Model} is contained within the
	 * {@link Simulation} of the particle
	 * 
	 * @return Model used in the particles Simulation
	 */

	public Model getModel() {
		return (this.simulation.getModel());
	}

	/**
	 * Deep copy this instance of {@link Particle}
	 * 
	 * @param reentrantLock
	 * 
	 * @return another instance of {@link Particle} with each fields within it
	 *         being isomorph to the original instance
	 */
	public Particle deepCopy(ReentrantLock lock) {

		Model mod = this.simulation.getModel().deepCopy();
		Simulation sim = new Simulation(mod);

		Map<String, GammaDistribution> newGammaDistribs = new HashMap<String, GammaDistribution>();

		for (String thisgamma : this.gammaDistribs.keySet()) {
			Double shape = this.gammaDistribs.get(thisgamma).getShape();
			Double scale = this.gammaDistribs.get(thisgamma).getScale();
			newGammaDistribs
					.put(thisgamma, new GammaDistribution(shape, scale));
		}

		Particle copy = new Particle(sim, lock);
		copy.gammaDistribs = newGammaDistribs;

		return copy;

	}

	/**
	 * Given the statistics from previous simulation run, update Gamma
	 * distribution that is associated with each Tunable within the
	 * {@link Model}, which is contained in this class' {@link Simulation}
	 * instance
	 * 
	 * @param stats
	 */

	private void gammaUpdate(SimulationStatistics stats) {

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
			double newScale = 1 / scale; // newScale == rate

			double rp = 0;
			double gp = 0;

			for (String reaction : reactions) {
				rp += stats.getExecutedNum().get(reaction);
				gp += stats.getPropSum().get(reaction);

			}

			gp = gp / this.simulation.getModel().getTunable().get(thisTunable);

			newShape += rp;
			newScale += gp;
			newScale = 1 / newScale;

			thisGamma = new GammaDistribution(newShape, newScale);
			this.gammaDistribs.put(thisTunable, thisGamma);

			if (Main.verbose) {
				System.out.println(thisTunable + " \t oldshape: " + shape
						+ "\t newshape: " + newShape + "\t oldscale: " + scale
						+ "\t newscale: " + newScale + "\t oldmean: "
						+ (shape * scale) + "\t newmean: "
						+ (newShape * newScale));
			}
		}
	}

	/**
	 * Assign new tunable values using underlying Gamma distribution associated
	 * with each tunable
	 */
	private void gammaSample() {

		Model model = this.simulation.getModel();
		HashMap<String, Double> tunables = model.getTunable();

		for (String thisTunable : tunables.keySet()) {

			GammaDistribution thisGamma = this.gammaDistribs.get(thisTunable);

			// gamma sample
			double newTunable = thisGamma.sample();
			this.simulation.getModel().setTunable(thisTunable, newTunable);
		}
	}

	/**
	 * Set the next simulation's run time. This method has to be called before
	 * running the thread!
	 * 
	 * @param t
	 */
	public void setRunSimulationTime(double t) {

		this.nextSimulationTime = t;
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

		gammaSample();
		SimulationStatistics stats = this.simulation.runSimulation(t);
		gammaUpdate(stats);

		return stats;
	}

	@Override
	public void run() {

		this.lock.lock();

		try {
			SimulationStatistics stat = runSimulation(this.nextSimulationTime);

			if (Main.verbose) {
				System.out.println("ExecutionNumber ProduceGata1 \t"
						+ stat.getExecutedNum().get("ProduceGata1"));
				System.out.println("ExecutionNumber ProducePu1 \t"
						+ stat.getExecutedNum().get("ProducePu1"));
				System.out.println("ExecutionNumber DegradeGata1 \t"
						+ stat.getExecutedNum().get("DegradeGata1"));
				System.out.println("ExecutionNumber DegradePu1 \t"
						+ stat.getExecutedNum().get("DegradePu1"));

				System.out.println("PropensitySum ProduceGata1 \t"
						+ stat.getPropSum().get("ProduceGata1"));
				System.out.println("PropensitySum ProducePu1 \t"
						+ stat.getPropSum().get("ProducePu1"));
				System.out.println("PropensitySum DegradeGata1 \t"
						+ stat.getPropSum().get("DegradeGata1"));
				System.out.println("PropensitySum DegradePu1 \t"
						+ stat.getPropSum().get("DegradePu1"));
			}
		} finally {
			this.lock.unlock();
		}

	}

}
