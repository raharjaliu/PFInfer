package filter;

import java.util.HashMap;

public class Model {

	HashMap<String, Double> species = new HashMap<String, Double>();
	HashMap<String, Double> constant = new HashMap<String, Double>();
	HashMap<String, Double> tunable = new HashMap<String, Double>();
	HashMap<String, HashMap<String, String>> reaction = new HashMap<String, HashMap<String, String>>();
	HashMap<String, String> propensity = new HashMap<String, String>();

	public void add_species(String name, Double value) {
		species.put(name, value);
	}

	public void add_constant(String name, Double value) {
		constant.put(name, value);
	}

	public void add_tunable(String name, Double value) {
		tunable.put(name, value);
	}

	public void add_reaction(String name, HashMap<String, String> reactionmap) {
		reaction.put(name, reactionmap);
	}

	public void add_species(String name, String expression) {
		propensity.put(name, expression);
	}

}
