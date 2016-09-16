package filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * 
 * Class used to perform the ParticleFilter Algorithm as described by Feigelmann
 * Justin (2011). "Stochastic and deterministic methods for the analysis of
 * Nanog dynamics in mouse embryonic stem cells" PhD thesis.
 * 
 * @author Pandu Raharja-Liu
 * @author Rene Schoeffel
 */

public class ParticleFilter {
	
	private static final String DEFAULT_CELL_ID = "DEFAULT_CELL_ID";

	public static double RUN_TIME = 100000;

	private ArrayList<Particle> particleList;
	private ArrayList<ReentrantLock> lockList;
	private HashMap<String, Integer> nameToColnumMap;
	private int threadNum;
	private int particleNum;
	private BufferedReader dbr;
	private Model baseModel;

	private double deviation = 5.0;

	/**
	 * Initializes {@link ParticleFilter}
	 * 
	 * @param model
	 *            Path to Model.xml file
	 * @param data
	 *            Path to Data file
	 * @param n
	 *            Number of Particles used in each Iteration
	 * @param _threadNum
	 *            Number of allowed threads
	 * 
	 */

	public ParticleFilter(String model, String data, int n, int _threadNum)
			throws IOException {

		File dataFile = new File(data);
		this.dbr = new BufferedReader(new FileReader(dataFile));

		String line = this.dbr.readLine();
		String[] cols = line.split("\t");
		this.nameToColnumMap = new HashMap<>();
		for (int i = 0; i < cols.length; i++) {
			this.nameToColnumMap.put(cols[i], i);
		}

		File modelFile = new File(model);
		Xmlparser generator = new Xmlparser();
		Model m = generator.generatemodel(modelFile);
		Simulation sim = new Simulation(m);

		// ReentrantLocks initialization and assignment
		this.threadNum = _threadNum;
		this.lockList = new ArrayList<ReentrantLock>();
		for (int i = 0; i < this.threadNum; i++) {
			this.lockList.add(new ReentrantLock());
		}

		int counter = 0;
		Particle particle = new Particle(sim, this.lockList.get(counter));
		particle.getModel().setCellID(ParticleFilter.DEFAULT_CELL_ID);

		this.particleList = new ArrayList<>();
		this.particleList.add(particle);
		for (int i = 1; i < n; i++) {
			counter += counter;
			this.particleList.add(particle.deepCopy(this.lockList.get(counter
					% this.lockList.size())));
		}

		this.particleNum = n;
		this.baseModel = m.deepCopy();

	}

	/**
	 * Perform the ParticleFilter Algorithm for instance of @ ParticleFilter}
	 */

	public void run() throws IOException {

		this.dbr.readLine();

		int dataPointCounter = 0;
		double last = Double
				.parseDouble(this.dbr.readLine().split("\t")[nameToColnumMap
						.get("time")]);
		String line;

		while ((line = this.dbr.readLine()) != null) {

			int particleCounter = 0;
			System.out.println("Data point #" + ++dataPointCounter);

			double current = Double
					.parseDouble(line.split("\t")[nameToColnumMap.get("time")]);
			double runTime = current - last;
			last = current;

			for (Particle p : this.particleList) {

				System.out.println("Running partile #" + ++particleCounter);

				p.getModel().setCellID(ParticleFilter.DEFAULT_CELL_ID);
				p.setRunSimulationTime(runTime);
				new Thread(p).start();

				// SimulationStatistics stat = p.runSimulation(runTime);

			}

			// checks whether all locks are free (i.e. all particle filters were
			// run)
			for (ReentrantLock l : this.lockList) {
				l.lock();
			}

			for (ReentrantLock l : this.lockList) {
				l.unlock();
			}

			// Creating Map of SpeciesDistribution for the current timestep

			HashMap<String, NormalDistribution> speciesDist = new HashMap<String, NormalDistribution>();
			String[] cols = line.split("\t");
			for (String colname : this.nameToColnumMap.keySet()) {
				Double mean = Double.parseDouble(cols[nameToColnumMap
						.get(colname)]);
				NormalDistribution temp = new NormalDistribution(mean,
						deviation);
				speciesDist.put(colname, temp);
			}
			speciesDist.remove("time");

			// Particle weighting
			ArrayList<Double> particleWeights = new ArrayList<>();

			Double weightSum = 0.0;

			for (Particle p : this.particleList) {
				Double combinedweight = 0.0;
				for (String species : speciesDist.keySet()) {
					combinedweight += speciesDist.get(species).density(
							p.getConcentration(species));
				}
				weightSum += combinedweight;
				particleWeights.add(combinedweight);
			}

			// Sample Particles based on particleWeights

			ArrayList<Particle> newParticleList = new ArrayList<>();

			HashSet<Integer> trackChosen = new HashSet<Integer>();

			int counter = 0;

			for (int i = 0; i < particleNum; i++) {
				Double weigthCutoff = (Math.random()) * weightSum;
				Double currentParticle = 0.0;
				for (int j = 0; j < particleWeights.size(); j++) {
					currentParticle += particleWeights.get(j);
					if (weigthCutoff < currentParticle) {
						ReentrantLock lock = this.lockList.get(counter
								% this.lockList.size());
						newParticleList.add(particleList.get(j).deepCopy(lock));
						trackChosen.add(j);
						break;
					}
				}
			}

			// Report Particle Sampling Collapse ratio
			
			System.out.println("Collapsed " + particleList.size()
					+ " particles onto " + trackChosen.size()
					+ " chosen particles");

			// Update ParticleList with newly sampled list
			this.particleList = newParticleList;

		}

		// Print tuned Particles
		
		printParticles();
	}
	
	private void printParticles(){
		
		for (int i = 0; i < particleNum; i++) {
			Particle p = this.particleList.get(i);
			Model simulated = p.getModel();
			System.out.println("\nParticle: " + (i+1));
			System.out.println("Species:");
			for (String s : simulated.getSpecies().keySet()) {
				System.out.println("\t" + s + "\t"
						+ baseModel.getSpecies().get(s) + "\t->\t"
						+ simulated.getSpecies().get(s));
			}
			System.out.println("Tunables:");
			for (String s : simulated.getTunable().keySet()) {
				System.out.println("\t" + s + "\t"
						+ baseModel.getTunable().get(s) + "\t->\t"
						+ simulated.getTunable().get(s));
			}

		}
		
	}

}
