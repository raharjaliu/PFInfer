package filter;

import java.util.HashMap;
import java.util.HashSet;

import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;

/**
 * 
 * Class representing a stochastical Model. A Model object is generated and
 * initialized through an instance of {@link Xmlparser}. This class contains
 * HashMaps for all components of the statistical Model.
 * 
 * @author Rene Schoeffel
 */
public class Model {
	
	private String cellID = "";

	private HashMap<String, Double> species = new HashMap<String, Double>();
	private HashMap<String, Double> constant = new HashMap<String, Double>();
	private HashMap<String, Double> tunable = new HashMap<String, Double>();
	private HashMap<String, HashMap<String, String>> reaction = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, String> propensity = new HashMap<String, String>();

	private HashMap<String, Expression> propensitymap = new HashMap<String, Expression>();
	private HashMap<String, HashMap<String, Expression>> reactionmap = new HashMap<String, HashMap<String, Expression>>();

	private Scope variablespace = Scope.create();

	private HashMap<String, HashMap<String, String>> dependencymap = new HashMap<String, HashMap<String, String>>();

	/**
	 * Maps all tunables contained within the tunable HashMap to a HashSet of
	 * reactions that rely on the individual tunable
	 * 
	 * @return HashMap mapping the name of tunable to a HashSet of reactions
	 *         that make use of the tunable
	 */

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

	/**
	 * Return the concentration of a given contained in this models' species
	 * HashMap
	 * 
	 * @param speciesname
	 * @return concentration of species
	 */

	public Double getSpecies(String name) {
		return this.species.get(name);
	}

	/**
	 * Set species - concentraion pair in this Models' species HashMap
	 * 
	 * @param speciesname
	 * @param speciesconcentration
	 */

	public void setSpecies(String name, Double value) {
		this.species.put(name, value);
	}

	public HashMap<String, Double> getSpecies() {
		return this.species;
	}
	
	/**
	 * Set constant - value pair in this Models' constant HashMap
	 * 
	 * @param constantname
	 * @param constantvalue
	 */

	public void setConstant(String name, Double value) {
		this.constant.put(name, value);
	}

	/**
	 * Set tunable - value pair in this Models' tunable HashMap
	 * 
	 * @param tunablename
	 * @param tunablevalue
	 */

	public void setTunable(String name, Double value) {
		this.tunable.put(name, value);
	}

	/**
	 * Return this Models' tunable HashMap
	 * 
	 * @return HashMap<String, Double> mapping tunable names to their values
	 */

	public HashMap<String, Double> getTunable() {
		return this.tunable;
	}

	/**
	 * Set reaction - reactionmap in this Models' reaction HashMap. The
	 * reactionmap maps the name of species changed by the reaction to the
	 * String representation of expression by which the species is change
	 * 
	 * @param reactionname
	 * @param reactionmap
	 *            mapping species to change expression
	 */

	public void setReaction(String name, HashMap<String, String> reactionmap) {
		this.reaction.put(name, reactionmap);
	}

	/**
	 * Set reaction - propensityexpression pair in this Models' propensity
	 * HashMap
	 * 
	 * @param reactioname
	 * @param propensityexpression
	 */

	public void setPropensity(String name, String expression) {
		this.propensity.put(name, expression);
	}

	/**
	 * Return this Models' propensity HashMap
	 * 
	 * @return HashMap<String, String> mapping reaction names to their
	 *         expression Strings
	 */

	public HashMap<String, String> getPropensities() {
		return this.propensity;
	}

	/**
	 * Creates Expression objects for each reaction in this Models' reaction
	 * HashMap and add them the reactionmap HashMap Creates Expression objects
	 * for each propensity in this Models' propensity HashMap and add them the
	 * propensitymap HashMap.
	 */

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

	/**
	 * Updates all species concentrations in the variablespace which is used by
	 * Expression Objects to calculate reactionpropensities and species changes
	 * of executed reactions
	 */

	public void updateSpeciesVariables() {
		for (String name : this.species.keySet()) {
			Variable temp = this.variablespace.getVariable(name);
			temp.setValue(this.species.get(name));
		}
	}

	/**
	 * Updates all constant values in the variablespace which is used by
	 * Expression Objects to calculate reactionpropensities and species changes
	 * of executed reactions. This method is only performed once by an instance
	 * of {@link Xmlparser} during the initialization of the Model object
	 */

	public void updateConstantVariables() {
		for (String name : this.constant.keySet()) {
			Variable temp = this.variablespace.getVariable(name);
			temp.setValue(this.constant.get(name));
		}
	}

	/**
	 * Updates all tunable values in the variablespace which is used by
	 * Expression Objects to calculate reactionpropensities and species changes
	 * of executed reactions.
	 */

	public void updateTunableVariables() {
		for (String name : this.tunable.keySet()) {
			Variable temp = this.variablespace.getVariable(name);
			temp.setValue(this.tunable.get(name));
		}
	}

	/**
	 * Executes an Reaction by evaluation all associated species changes
	 * contained in this Models' reactionmap
	 * 
	 * @param name
	 *            name of the executed reaction
	 * @param time
	 *            time for which the reaction is executed
	 */

	public void executeReaction(String name, Double time) {

		HashMap<String, Expression> changemap = this.reactionmap.get(name);

		for (String key : changemap.keySet()) {
			Double old = this.species.get(key);
			Double change = time * changemap.get(key).evaluate();
			Double newValue = old + change;

			if (newValue < 0) {
				System.err.println("species " + key + "  negative value "
						+ newValue + " replaced with 0.0");
				newValue = 0.0;
			}

			this.species.put(key, newValue);
			Variable temp = this.variablespace.getVariable(key);
			temp.setValue(newValue);

		}
		this.updateSpeciesVariables();
	}

	/**
	 * Return the value of a propensity expression of a given reaction contained
	 * in this models' propensity HashMap. The propensity value is calculated
	 * via its Expression Object in the propensitymap HashMap
	 * 
	 * @param reactioname
	 * @return propensity calculated from it's propensity expression given the
	 *         current species, tunables and constants
	 */

	public Double getPropensity(String name) {
		Expression expr = this.propensitymap.get(name);
		return (expr.evaluate());
	}

	/**
	 * Updates this Models depencencymap HashMap, by evaluation for each
	 * reaction which propensities rely on the changed species
	 */

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

	/**
	 * Return this Models' dependencymap HashMap
	 * 
	 * @return HashMap<String, HashMap<String, String>> mapping tunable reaction
	 *         to map of propensities which need to be updated after execution
	 *         of the reaction
	 */

	public HashMap<String, HashMap<String, String>> getDepencyMap() {
		return (this.dependencymap);
	}

	/**
	 * Deep copy this instance of {@link Model}
	 * 
	 * @return another instance of {@link Model} with each HashMap within it
	 *         being isomorph to the original instance
	 */

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
		
		outmodel.setCellID(this.cellID);

		return outmodel;
	}

	/**
	 * Internal method used only by the deepcopy method to overwrite an the
	 * species HashMap of a freshly generated instance of {@link Model} with a
	 * copy of the species HashMap form the instance it is copied from*
	 */

	private void overwriteSpecies(HashMap<String, Double> copy) {
		this.species = copy;
	}

	/**
	 * Internal method used only by the deepcopy method to overwrite an the
	 * constant HashMap of a freshly generated instance of {@link Model} with a
	 * copy of the constant HashMap form the instance it is copied from*
	 */

	private void overwriteConstant(HashMap<String, Double> copy) {
		this.constant = copy;
	}

	/**
	 * Internal method used only by the deepcopy method to overwrite an the
	 * tunable HashMap of a freshly generated instance of {@link Model} with a
	 * copy of the tunable HashMap form the instance it is copied from*
	 */

	private void overwriteTunable(HashMap<String, Double> copy) {
		this.tunable = copy;
	}

	/**
	 * Internal method used only by the deepcopy method to overwrite an the
	 * reaction HashMap of a freshly generated instance of {@link Model} with a
	 * copy of the reaction HashMap form the instance it is copied from*
	 */

	private void overwriteReaction(HashMap<String, HashMap<String, String>> copy) {
		this.reaction = copy;
	}

	/**
	 * Internal method used only by the deepcopy method to overwrite an the
	 * propensity HashMap of a freshly generated instance of {@link Model} with
	 * a copy of the propensity HashMap form the instance it is copied from*
	 */

	private void overwritePropensity(HashMap<String, String> copy) {
		this.propensity = copy;
	}

	/**
	 * Internal method used only by the deepcopy method to overwrite an the
	 * dependencymap HashMap of a freshly generated instance of {@link Model}
	 * with a copy of the dependencymap HashMap form the instance it is copied
	 * from*
	 */

	private void overwriteDependency(
			HashMap<String, HashMap<String, String>> copy) {
		this.dependencymap = copy;
	}
	
	/**
	 * Setter method for {@link #cellID}
	 * 
	 * @param _cellID new cell ID to be assigned
	 */
	public void setCellID(String _cellID) {
		this.cellID = _cellID;
	}
	
	/**
	 * Getter method for {@link #cellID}
	 * @return
	 */
	public String getCellID() {
		return this.cellID;
	}

}
