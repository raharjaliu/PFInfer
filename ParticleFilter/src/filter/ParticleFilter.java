package filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
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
	private int threadNum;
	private int particleNum;
	private Model baseModel;

	private HashMap<String, HashMap<String, HashMap<String, ArrayList<Double>>>> dataTrees;

	private double deviation = 0.01;

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

		// parse and store time series data trees
		File dataFile = new File(data);
		Dataparser dataparser = new Dataparser();
		this.dataTrees = dataparser.generateDataTrees(dataFile);

		// generate basemodel
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

	public void run() throws IOException {

		// iterate over data trees
		for (String treeID : this.dataTrees.keySet()) {

			HashMap<String, HashMap<String, ArrayList<Double>>> tree = this.dataTrees
					.get(treeID);

			if (Main.outfile != null) {
				String header = "Tree\tCell\tParticle";
				for (String s : baseModel.getTunable().keySet()) {
					header = header + "\t" + s;
				}
				Main.outfile.write(header + "\n");
			}

			// skip trees that contain little Data
			if (tree.size() <= 10) {
				System.out.println("Skipping Tree: " + treeID + "\t Size: "
						+ tree.size());
			} else {

				// build Queue from data
				QueueBuilder builder = new QueueBuilder(tree.keySet());
				Queue<String> cellorder = builder.getQueue();
				HashSet<String> leaves = builder.getLeaves();
				HashSet<String> branches = builder.getBranches();

				System.out.println("Running Tree: " + treeID + "\t Size: "
						+ tree.size() + "\t Branches: " + branches.size()
						+ "\t Leaves: " + leaves.size());

				// Create new particleListMap for this tree
				HashMap<String, ArrayList<Particle>> particleListMap = new HashMap<String, ArrayList<Particle>>();
				ArrayList<Particle> currentParticles = deepCopyParticleList(
						this.particleList, "0");
				particleListMap.put("0", currentParticles);

				// iterate over cellQueue
				while (cellorder.isEmpty() != true) {

					String currentCell = cellorder.poll();
					String parent = builder.getParent(currentCell);
					ArrayList<Particle> currentList = deepCopyParticleList(
							particleListMap.get(parent), currentCell);

					// get Branch Data
					HashMap<String, ArrayList<Double>> currentCellData = tree
							.get(currentCell);
					Double lastTime = currentCellData.get("time").get(0);

					// Iterate over Branch Data
					for (int i = 1; i < currentCellData.get("time").size(); i++) {
						System.out.println("Running Tree " + treeID + " Cell "
								+ currentCell + " ("
								+ (tree.size() - cellorder.size()) + "/"
								+ tree.size() + ") " + "Datapoint " + i + "/"
								+ (currentCellData.get("time").size() - 1));
						Double currentTime = currentCellData.get("time").get(i);
						Double runTime = currentTime - lastTime;
						lastTime = currentTime;

						// start Particle Threads
						int particleCounter = 0;
						for (Particle p : currentList) {
							if (Main.verbose) {
								System.out.println("Running partile #"
										+ ++particleCounter);
							}
							p.setRunSimulationTime(runTime);
							new Thread(p).start();
						}

						// checks whether all locks are free (i.e. all particle
						// filters were run)
						for (ReentrantLock l : this.lockList) {
							l.lock();
						}
						for (ReentrantLock l : this.lockList) {
							l.unlock();
						}

						// Creating Map of SpeciesDistribution for the current
						// timestep
						HashMap<String, NormalDistribution> speciesDist = new HashMap<String, NormalDistribution>();

						for (String species : this.baseModel.getSpecies()
								.keySet()) {
							Double mean = currentCellData.get(species).get(i);
							NormalDistribution temp = new NormalDistribution(
									mean, deviation);
							speciesDist.put(species, temp);
						}
						// weight Particles
						ArrayList<Double> particleWeights = weightParticles(
								currentList, speciesDist);

						// sample Particles
						ArrayList<Particle> newParticleList = sampleParticles(
								currentList, particleWeights);

						// Update ParticleList with newly sampled list
						currentList = newParticleList;

					}

					// Update particleListMap with finished cell simulation
					particleListMap.put(currentCell, currentList);

				}

				if (Main.outfile != null) {
					System.out.println("Writing Output to: "
							+ Main.outfile.toString());
				}

				for (String leaf : leaves) {
					if (Main.outfile == null) {
						System.out.println("Tree " + treeID + " Cell " + leaf);
					}
					// printParticles(particleListMap.get(leaf));
					printParticlesTublar(particleListMap.get(leaf), treeID,
							leaf);
				}

				if (Main.outfile != null) {
					Main.outfile.close();
				}

				for (String leaf : leaves) {
					printParticles(particleListMap.get(leaf));
				}
			}
		}
	}

	private ArrayList<Particle> deepCopyParticleList(
			ArrayList<Particle> _particleList, String cellID) {

		ArrayList<Particle> newList = new ArrayList<Particle>();

		for (int i = 0; i < this.particleNum; i++) {
			Particle newparticle = _particleList.get(i).deepCopy(
					this.lockList.get(i % this.lockList.size()));
			newparticle.getModel().setCellID(cellID);
			newList.add(newparticle);
		}

		return (newList);

	}

	private ArrayList<Double> weightParticles(
			ArrayList<Particle> _particleList,
			HashMap<String, NormalDistribution> _speciesDist) {

		ArrayList<Double> _particleWeights = new ArrayList<>();

		for (Particle p : _particleList) {
			Double combinedweight = 0.0;
			for (String species : _speciesDist.keySet()) {
				combinedweight += _speciesDist.get(species).density(
						p.getConcentration(species));
			}
			_particleWeights.add(combinedweight);
		}

		return (_particleWeights);
	}

	private ArrayList<Particle> sampleParticles(
			ArrayList<Particle> _particleList,
			ArrayList<Double> _particleWeights) {

		ArrayList<Particle> newParticleList = new ArrayList<>();

		HashSet<Integer> trackChosen = new HashSet<Integer>();

		Double weightSum = 0.0;

		for (int i = 0; i < particleNum; i++) {

			weightSum += _particleWeights.get(i);
		}

		int counter = 0;

		for (int i = 0; i < particleNum; i++) {
			Double weigthCutoff = (Math.random()) * weightSum;
			Double currentParticle = 0.0;
			for (int j = 0; j < _particleWeights.size(); j++) {
				currentParticle += _particleWeights.get(j);
				if (weigthCutoff < currentParticle) {
					ReentrantLock lock = this.lockList.get(counter
							% this.lockList.size());
					newParticleList.add(_particleList.get(j).deepCopy(lock));
					trackChosen.add(j);
					break;
				}
			}
		}

		// Report Particle Sampling Collapse ratio

		System.out
				.println("Collapsed " + _particleList.size()
						+ " particles onto " + trackChosen.size()
						+ " chosen particles");

		return (newParticleList);
	}

	private void printParticlesTublar(ArrayList<Particle> _particleList,
			String _treeID, String _cellID) {

		String line = "Tree\tCell\tParticle";
		for (String s : baseModel.getTunable().keySet()) {
			line = line + "\t" + s;
		}

		if (Main.outfile == null) {
			System.out.println(line);
		}

		for (int i = 0; i < particleNum; i++) {
			Particle p = _particleList.get(i);
			Model simulated = p.getModel();
			line = _treeID + "\t" + _cellID + "\t" + Integer.toString(i);
			for (String s : baseModel.getTunable().keySet()) {
				line = line + "\t" + simulated.getTunable().get(s);
			}
			if (Main.outfile != null) {
				Main.outfile.write(line + "\n");
			} else {
				System.out.println(line);
			}
		}
	}

	private void printParticles(ArrayList<Particle> _particleList) {

		for (int i = 0; i < particleNum; i++) {
			Particle p = _particleList.get(i);
			Model simulated = p.getModel();
			System.out.println("\nParticle: " + (i + 1));
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
