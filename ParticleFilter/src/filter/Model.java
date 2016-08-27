package filter;

import java.util.HashMap;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import expr.Variable;

public class Model {

	private HashMap<String, Double> species = new HashMap<String, Double>();
	private HashMap<String, Double> constant = new HashMap<String, Double>();
	private HashMap<String, Double> tunable = new HashMap<String, Double>();
	private HashMap<String, HashMap<String, String>> reaction = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, String> propensity = new HashMap<String, String>();

	private HashMap<String, Expr> propensitymap = new HashMap<String, Expr>();
	private HashMap<String, HashMap<String, Expr>> reactionmap = new HashMap<String, HashMap<String, Expr>>();

	public void setSpecies(String name, Double value) {
		species.put(name, value);
	}

	public void setConstant(String name, Double value) {
		constant.put(name, value);
	}

	public void setTunable(String name, Double value) {
		tunable.put(name, value);
	}

	public void setReaction(String name, HashMap<String, String> reactionmap) {
		reaction.put(name, reactionmap);
	}

	public void setPropensity(String name, String expression) {
		propensity.put(name, expression);
	}

	public void pepareEvaluators() {
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

	public void updateSpeciesVariables() {
		for (String name : species.keySet()) {
			Variable temp = Variable.make(name);
			temp.setValue(species.get(name));
		}
	}

	public void updateConstantVariables() {
		for (String name : constant.keySet()) {
			Variable temp = Variable.make(name);
			temp.setValue(constant.get(name));
		}
	}

	public void updateTunableVariables() {
		for (String name : tunable.keySet()) {
			Variable temp = Variable.make(name);
			temp.setValue(tunable.get(name));
		}
	}

	public void executeReaction(String name, Double time) {
		HashMap<String, Expr> changemap = reactionmap.get(name);
		for (String key : changemap.keySet()) {
			Double old = species.get(key);
			//TODO: Fix Model so each has its own Variable Space
			this.updateSpeciesVariables();
			this.updateTunableVariables();
			//remove these updates after fix
			Double change = time * changemap.get(key).value();
			System.out.println(old + "\t" + change +"\t" + (old +change));
			species.put(key, old + change);
		}
		this.updateSpeciesVariables();
	}

	public Double getPropensity(String name) {
		Expr expr = propensitymap.get(name);
		return (expr.value());
	}

	public HashMap<String, String> getPropensities() {
		return propensity;
	}

	public Model deepCopy() {
		Model outmodel = new Model();

		HashMap<String, Double> speciescopy = new HashMap<String, Double>(
				species);
		HashMap<String, Double> constantcopy = new HashMap<String, Double>(
				constant);
		HashMap<String, Double> tunablecopy = new HashMap<String, Double>(
				tunable);
		HashMap<String, HashMap<String, String>> reactioncopy = new HashMap<String, HashMap<String, String>>(
				reaction);
		HashMap<String, String> propensitycopy = new HashMap<String, String>(
				propensity);
		outmodel.overwriteSpecies(speciescopy);
		outmodel.overwriteConstant(constantcopy);
		outmodel.overwriteTunable(tunablecopy);
		outmodel.overwriteReaction(reactioncopy);
		outmodel.overwritePropensity(propensitycopy);

		outmodel.pepareEvaluators();
		outmodel.updateSpeciesVariables();
		outmodel.updateConstantVariables();
		outmodel.updateTunableVariables();

		return outmodel;
	}

	private void overwriteSpecies(HashMap<String, Double> copy) {
		species = copy;
	}

	private void overwriteConstant(HashMap<String, Double> copy) {
		constant = copy;
	}

	private void overwriteTunable(HashMap<String, Double> copy) {
		tunable = copy;
	}

	private void overwriteReaction(HashMap<String, HashMap<String, String>> copy) {
		reaction = copy;
	}

	private void overwritePropensity(HashMap<String, String> copy) {
		propensity = copy;
	}

}
