package filter;

import java.util.HashMap;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import expr.Variable;

public class Model {

	HashMap<String, Double> species = new HashMap<String, Double>();
	HashMap<String, Double> constant = new HashMap<String, Double>();
	HashMap<String, Double> tunable = new HashMap<String, Double>();
	HashMap<String, HashMap<String, String>> reaction = new HashMap<String, HashMap<String, String>>();
	HashMap<String, String> propensity = new HashMap<String, String>();

	HashMap<String, Expr> propensitymap = new HashMap<String, Expr>();
	HashMap<String, HashMap<String, Expr>> reactionmap = new HashMap<String, HashMap<String, Expr>>();

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

	public void pepare_evaluators() {
		Parser parser = new Parser();
		for (String name : propensity.keySet()) {
			String expression = propensity.get(name);
			Expr expr = null;
			try {
				expr = parser.parseString(expression);
			} catch (SyntaxException e) {
				System.out.println(e);
				System.exit(1);
			}
			propensitymap.put(name, expr);
		}
		for (String name : reaction.keySet()) {
			HashMap<String, String> changemap = reaction.get(name);
			HashMap<String, Expr> expressionmap = new HashMap<String, Expr>();
			for (String key : changemap.keySet()) {
				String expression = changemap.get(key);
				Expr expr = null;
				try {
					expr = parser.parseString(expression);
				} catch (SyntaxException e) {
					System.out.println(e);
					System.exit(1);
				}
				expressionmap.put(key, expr);
			}
			reactionmap.put(name, expressionmap);
		}
	}

	public void update_species_variables() {
		for (String name : species.keySet()) {
			Variable temp = Variable.make(name);
			temp.setValue(species.get(name));
		}
	}

	public void update_constant_variables() {
		for (String name : constant.keySet()) {
			Variable temp = Variable.make(name);
			temp.setValue(constant.get(name));
		}
	}

	public void update_tunable_variables() {
		for (String name : tunable.keySet()) {
			Variable temp = Variable.make(name);
			temp.setValue(tunable.get(name));
		}
	}

	public Double get_propensity(String name) {
		Expr expr = propensitymap.get(name);
		return (expr.value());
	}

	public void execute_reaction(String name, Double time) {
		HashMap<String, Expr> changemap = reactionmap.get(name);
		for (String key : changemap.keySet()) {
			Double old = species.get(key);
			Double change = time *changemap.get(key).value();
			//System.out.println(old + "\t" + change +"\t" + (old +change));
			species.put(key, old + change);			
		}
		this.update_species_variables();
	}

}
