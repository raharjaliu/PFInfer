package filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Dataparser {
	
	//private double scaleGata = 10000.0;
	//private double scalePu = 12500.0;
	
	private double scaleGata = 1.0;
	private double scalePu = 1.0;

	public HashMap<String, HashMap<String, HashMap<String, ArrayList<Double>>>> generateDataTrees(
			File dataFile) throws IOException {

		HashMap<String, HashMap<String, HashMap<String, ArrayList<Double>>>> treeMap = new HashMap<String, HashMap<String, HashMap<String, ArrayList<Double>>>>();

		BufferedReader dbr = new BufferedReader(new FileReader(dataFile));

		HashMap<String, Integer> nameToColnumMap;

		String line = dbr.readLine();
		
		String[] cols = line.split(",");
		nameToColnumMap = new HashMap<>();
		for (int i = 0; i < cols.length; i++) {
			nameToColnumMap.put(cols[i], i);
		}

		while ((line = dbr.readLine()) != null) {
			cols = line.split(",");
			String treeID = cols[nameToColnumMap.get("treeID")];

			if (treeMap.containsKey(treeID) == false) {
				HashMap<String, HashMap<String, ArrayList<Double>>> newtree = new HashMap<String, HashMap<String, ArrayList<Double>>>();
				treeMap.put(treeID, newtree);
			}

			String cellNr = cols[nameToColnumMap.get("cellNr")];
			if (treeMap.get(treeID).containsKey(cellNr) == false) {
				HashMap<String, ArrayList<Double>> newcell = new HashMap<String, ArrayList<Double>>();
				newcell.put("time", new ArrayList<Double>());
				newcell.put("Gata1", new ArrayList<Double>());
				newcell.put("Pu1", new ArrayList<Double>());
				treeMap.get(treeID).put(cellNr, newcell);

			}

			HashMap<String, ArrayList<Double>> currentCell = treeMap
					.get(treeID).get(cellNr);
			double gata = scaleGata * (Double.parseDouble(cols[nameToColnumMap.get("auto_w02")]));
			double pu = scalePu * (Double.parseDouble(cols[nameToColnumMap.get("auto_w01")]));
			double time = Double.parseDouble(cols[nameToColnumMap.get("absoluteTime")]);
			currentCell.get("time").add(time);
			currentCell.get("Gata1").add(gata);
			currentCell.get("Pu1").add(pu);
			
			
			//currentCell.get("Gata1").add((Double.parseDouble(cols[nameToColnumMap.get("auto_w02")]))/(Double.parseDouble(cols[nameToColnumMap.get("auto_w02_area")])));
			//currentCell.get("Pu1").add((Double.parseDouble(cols[nameToColnumMap.get("auto_w01")]))/(Double.parseDouble(cols[nameToColnumMap.get("auto_w01_area")])));
		}
		dbr.close();

		System.out.println("Number of Timeseries Trees: " + treeMap.size());
		return (treeMap);

	}

}
