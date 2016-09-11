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

	public static double RUN_TIME = 100000;

	private ArrayList<Particle> particleList;
	private ArrayList<ReentrantLock> lockList;
	private HashMap<String, Integer> nameToColnumMap;
	private int threadNum;
	private int particleNum;
	private BufferedReader dbr;

	private double deviation = 2.0;

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
		this.lockList = new ArrayList<ReentrantLock>();
		for (int i = 0; i < this.threadNum; i++) {
			this.lockList.add(new ReentrantLock());
		}
		
		int counter = 0;
		Particle particle = new Particle(sim, this.lockList.get(counter));

		this.particleList = new ArrayList<>();
		this.particleList.add(particle);
		for (int i = 1; i < n; i++) {
			counter += counter; 
			this.particleList.add(particle.deepCopy(this.lockList.get(counter % this.lockList.size())));
		}

		this.particleNum = n;
		this.threadNum = _threadNum;
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

			// TODO fetch next value

			int particleCounter = 0;
			System.out.println("Data point #" + ++dataPointCounter);

			double current = Double
					.parseDouble(line.split("\t")[nameToColnumMap.get("time")]);
			double runTime = current - last;
			last = current;

			for (Particle p : this.particleList) {

				System.out.println("Running partile #" + ++particleCounter);

				p.setRunSimulationTime(runTime);
				new Thread(p).start();
				
//				SimulationStatistics stat = p.runSimulation(runTime);

			}
			
			// checks whether all locks are free (i.e. all particle filters were run)
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
			// TODO assert that order of particles in particleList and
			// particleWeights are synchronized
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

			for (int i = 0; i < particleList.size(); i++) {
				Double weigthCutoff = (Math.random()) * weightSum;
				Double currentParticle = 0.0;
				for (int j = 0; j < particleWeights.size(); j++) {
					currentParticle += particleWeights.get(j);
					if (weigthCutoff < currentParticle) {
						ReentrantLock lock = this.lockList.get(counter % this.lockList.size());
						newParticleList.add(particleList.get(j).deepCopy(lock));
						trackChosen.add(j);
						break;
					}
				}
			}

			System.out.println("Collapsed " + particleList.size()
					+ " particles onto " + trackChosen.size()
					+ " chosen particles");

			// THIS Works
			// But eventually Kdg and Kdp get so big that they degrade more than
			// is available resulting in negative concentration ...
			// resulting in negative propensities .... resulting in no reaction
			// being chosen ... resulting in an null pointer exception

			//this.particleList = newParticleList;
			
			

		}

	}

}
