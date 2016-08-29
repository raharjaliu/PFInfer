package filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ParticleFilter {

	public static double RUN_TIME = 100000;

	private ArrayList<Particle> particleList;
	private HashMap<String, Integer> nameToColnumMap;
	private int threadNum;
	private int particleNum;
	private BufferedReader dbr;

	public ParticleFilter(String model, String data, int n, int _threadNum)
			throws IOException {

		
		File dataFile = new File(data);
		this.dbr = new BufferedReader(new FileReader(dataFile));
		
		String line = this.dbr.readLine();
		String[] cols = line.split("\t");
		this.nameToColnumMap = new HashMap<>();
		for (int i = 1; i < cols.length; i++) {
			this.nameToColnumMap.put(cols[i], i);
		}
		
		File modelFile = new File(model);
		Xmlparser generator = new Xmlparser();
		Model m = generator.generatemodel(modelFile);
		Simulation sim = new Simulation(m);
		Particle particle = new Particle(sim);

		this.particleList = new ArrayList<>();
		this.particleList.add(particle);
		for (int i = 1; i < n; i++) {
			this.particleList.add(particle.deepCopy());
		}

		this.particleNum = n;
		this.threadNum = _threadNum;
	}

	public void run() throws IOException {

		this.dbr.readLine();
		
		int dataPointCounter = 0;
		double last = Double.parseDouble(this.dbr.readLine().split("\t")[0]);
		String line;

		while ((line = this.dbr.readLine()) != null) {
			
			// TODO fetch next value

			int particleCounter = 0;
			System.out.println("Data point #" + ++dataPointCounter);
			
			double current = Double.parseDouble(line.split("\t")[0]);
			double runTime = current - last;
			last = current;

			for (Particle p : this.particleList) {
				

				System.out.println("Running partile #" + ++particleCounter);

				SimulationStatistics stat = p.runSimulation(runTime);

				System.out.println(stat.getExecutedNum().get("ProduceGata1"));
				System.out.println(stat.getExecutedNum().get("ProducePu1"));
				System.out.println(stat.getExecutedNum().get("DegradeGata1"));
				System.out.println(stat.getExecutedNum().get("DegradePu1"));

				System.out.println(stat.getPropSum().get("ProduceGata1"));
				System.out.println(stat.getPropSum().get("ProducePu1"));
				System.out.println(stat.getPropSum().get("DegradeGata1"));
				System.out.println(stat.getPropSum().get("DegradePu1"));
			}
			
			// TODO particle weighting
			
			// TODO particle "refill"/reselection
			
		}

	}

}
