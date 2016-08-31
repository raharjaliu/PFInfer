package filter;

import java.util.HashMap;
import java.util.HashSet;

import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;

public class Model {

	private HashMap<String, Double> species = new HashMap<String, Double>();
	private HashMap<String, Double> constant = new HashMap<String, Double>();
	private HashMap<String, Double> tunable = new HashMap<String, Double>();
	private HashMap<String, HashMap<String, String>> reaction = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, String> propensity = new HashMap<String, String>();

	private HashMap<String, Expression> propensitymap = new HashMap<String, Expression>();
	private HashMap<String, HashMap<String, Expression>> reactionmap = new HashMap<String, HashMap<String, Expression>>();

	private Scope variablespace = Scope.create();

	private HashMap<String, HashMap<String, String>> dependencymap = new HashMap<String, HashMap<String, String>>();

	public HashMap<String, HashSet<String>> getTunableReactionMap() {

		HashMap<String, HashSet<String>> outmap = new HashMap<String, HashSet<String>>();

		for (String tunablename : this.tunable.keySet()) {
			HashSet<String> reactionMap = new HashSet<String>();
			for (String reactioname : this.reaction.keySet()) {
				for (String subreaction : this.reaction.get(reactioname)
						.keySet()) {
					String expression = this.reaction.get(reactioname).get(
							subreaction);
					if (expression.contains(tunablename)) {
						reactionMap.add(reactioname);
						// System.out.println(tunablename + "\t" + reactioname);
					}
				}
			}
			outmap.put(tunablename, reactionMap);
		}
		return outmap;
	}
	
	public Double getSpecies (String name){
		return this.species.get(name);
	}

	public void setSpecies(String name, Double value) {
		this.species.put(name, value);
	}

	public void setConstant(String name, Double value) {
		this.constant.put(name, value);
	}

	public void setTunable(String name, Double value) {
		this.tunable.put(name, value);
	}

	public HashMap<String, Double> getTunable() {
		return this.tunable;
	}

	public void setReaction(String name, HashMap<String, String> reactionmap) {
		this.reaction.put(name, reactionmap);
	}

	public void setPropensity(String name, String expression) {
		this.propensity.put(name, expression);
	}

	public void pepareEvaluators() {
		for (String name : this.propensity.keySet()) {
			String expression = this.propensity.get(name);
			Expression expr = null;
			try {
				expr = Parser.parse(expression, this.variablespace);
			} catch (ParseException e) {
				System.out.println(e);
				System.exit(1);
			}
			this.propensitymap.put(name, expr);
		}
		for (String name : this.reaction.keySet()) {
			HashMap<String, String> changemap = this.reaction.get(name);
			HashMap<String, Expression> expressionmap = new HashMap<String, Expression>();
			for (String key : changemap.keySet()) {
				String expression = changemap.get(key);
				Expression expr = null;
				try {
					expr = Parser.parse(expression, this.variablespace);
				} catch (ParseException e) {
					System.out.println(e);
					System.exit(1);
				}
				expressionmap.put(key, expr);
			}
			this.reactionmap.put(name, expressionmap);
		}
	}

	public void updateSpeciesVariables() {
		for (String name : this.species.keySet()) {
			Variable temp = this.variablespace.getVariable(name);
			temp.setValue(this.species.get(name));
		}
	}

	public void updateConstantVariables() {
		for (String name : this.constant.keySet()) {
			Variable temp = this.variablespace.getVariable(name);
			temp.setValue(this.constant.get(name));
		}
	}

	public void updateTunableVariables() {
		for (String name : this.tunable.keySet()) {
			Variable temp = this.variablespace.getVariable(name);
			temp.setValue(this.tunable.get(name));
		}
	}

	public void executeReaction(String name, Double time) {
		HashMap<String, Expression> changemap = this.reactionmap.get(name);
		for (String key : changemap.keySet()) {
			Double old = this.species.get(key);		
			Double change = time * changemap.get(key).evaluate();
			Double newValue = old + change;
			if(newValue < 0){
				System.err.println("something is fishy!!! species " + key +"  below 0.0");
			}
			
			this.species.put(key, newValue);
			Variable temp = this.variablespace.getVariable(key);
			temp.setValue(newValue);

		}
		this.updateSpeciesVariables();
	}

	public Double getPropensity(String name) {
		Expression expr = this.propensitymap.get(name);
		return (expr.evaluate());
	}

	public HashMap<String, String> getPropensities() {
		return this.propensity;
	}

	public void updateDependency() {
		for (String key : reaction.keySet()) {
			String tag = key;
			HashMap<String, String> dependent = new HashMap<String, String>();

			for (String name : this.reaction.get(key).keySet()) {
				for (String prop : this.propensity.keySet()) {
					String expression = this.propensity.get(prop);
					if (expression.contains(name)) {
						dependent.put(prop, name);
					}
				}
			}
			this.dependencymap.put(tag, dependent);
		}
	}

	public HashMap<String, HashMap<String, String>> getDepencyMap() {
		return (this.dependencymap);
	}

	public Model deepCopy() {
		Model outmodel = new Model();

		HashMap<String, Double> speciescopy = new HashMap<String, Double>(
				this.species);
		HashMap<String, Double> constantcopy = new HashMap<String, Double>(
				this.constant);
		HashMap<String, Double> tunablecopy = new HashMap<String, Double>(
				this.tunable);
		HashMap<String, HashMap<String, String>> reactioncopy = new HashMap<String, HashMap<String, String>>(
				this.reaction);
		HashMap<String, String> propensitycopy = new HashMap<String, String>(
				this.propensity);
		HashMap<String, HashMap<String, String>> dependencycopy = new HashMap<String, HashMap<String, String>>(
				this.dependencymap);

		outmodel.overwriteSpecies(speciescopy);
		outmodel.overwriteConstant(constantcopy);
		outmodel.overwriteTunable(tunablecopy);
		outmodel.overwriteReaction(reactioncopy);
		outmodel.overwritePropensity(propensitycopy);
		outmodel.overwriteDependency(dependencycopy);

		outmodel.pepareEvaluators();
		outmodel.updateSpeciesVariables();
		outmodel.updateConstantVariables();
		outmodel.updateTunableVariables();

		return outmodel;
	}

	private void overwriteSpecies(HashMap<String, Double> copy) {
		this.species = copy;
	}

	private void overwriteConstant(HashMap<String, Double> copy) {
		this.constant = copy;
	}

	private void overwriteTunable(HashMap<String, Double> copy) {
		this.tunable = copy;
	}

	private void overwriteReaction(HashMap<String, HashMap<String, String>> copy) {
		this.reaction = copy;
	}

	private void overwritePropensity(HashMap<String, String> copy) {
		this.propensity = copy;
	}

	private void overwriteDependency(
			HashMap<String, HashMap<String, String>> copy) {
		this.dependencymap = copy;
	}

}
