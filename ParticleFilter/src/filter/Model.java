package filter;

import java.util.HashMap;

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

	public void updateDependency() {
		for (String key : reaction.keySet()) {
			String tag = key;
			HashMap<String, String> dependent = new HashMap<String, String>();
			
			for (String name : reaction.get(key).keySet()) {
				for (String prop : propensity.keySet()){
					String expression = propensity.get(prop);
					if (expression.contains(name)){
						dependent.put(prop,name);
					}
				}
			}
			dependencymap.put(tag, dependent);
		}
	}
	
	public HashMap<String, HashMap<String, String>> getDepencyMap(){
		
		return(this.dependencymap);
	}

	public void setSpecies(String name, Double value) {
		species.put(name, value);
	}

	public void setConstant(String name, Double value) {
		constant.put(name, value);
	}

	public void setTunable(String name, Double value) {
		tunable.put(name, value);
	}

	public HashMap<String, Double> getTunable() {
		return this.tunable;
	}

	public void setReaction(String name, HashMap<String, String> reactionmap) {
		reaction.put(name, reactionmap);
	}

	public void setPropensity(String name, String expression) {
		propensity.put(name, expression);
	}

	public void pepareEvaluators() {
		for (String name : propensity.keySet()) {
			String expression = propensity.get(name);
			Expression expr = null;
			try {
				expr = Parser.parse(expression, variablespace);
			} catch (ParseException e) {
				System.out.println(e);
				System.exit(1);
			}
			propensitymap.put(name, expr);
		}
		for (String name : reaction.keySet()) {
			HashMap<String, String> changemap = reaction.get(name);
			HashMap<String, Expression> expressionmap = new HashMap<String, Expression>();
			for (String key : changemap.keySet()) {
				String expression = changemap.get(key);
				Expression expr = null;
				try {
					expr = Parser.parse(expression, variablespace);
				} catch (ParseException e) {
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
			Variable temp = variablespace.getVariable(name);
			temp.setValue(species.get(name));
		}
	}

	public void updateConstantVariables() {
		for (String name : constant.keySet()) {
			Variable temp = variablespace.getVariable(name);
			temp.setValue(constant.get(name));
		}
	}

	public void updateTunableVariables() {
		for (String name : tunable.keySet()) {
			Variable temp = variablespace.getVariable(name);
			temp.setValue(tunable.get(name));
		}
	}

	public void executeReaction(String name, Double time) {
		HashMap<String, Expression> changemap = reactionmap.get(name);
		for (String key : changemap.keySet()) {
			Double old = species.get(key);
			Double change = time * changemap.get(key).evaluate();
			Double newValue = old + change;
			// System.out.println(old + "\t" + change +"\t" + newValue);
			species.put(key, newValue);
			Variable temp = variablespace.getVariable(key);
			temp.setValue(newValue);

		}
		this.updateSpeciesVariables();
	}

	public Double getPropensity(String name) {
		Expression expr = propensitymap.get(name);
		return (expr.evaluate());
	}

	public HashMap<String, String> getPropensities() {
		return this.propensity;
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
