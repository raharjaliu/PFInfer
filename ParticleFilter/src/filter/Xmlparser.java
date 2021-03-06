package filter;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Class used to create and initialize an instance of {@link Model} by parsing
 * an Model.xml file, which contains the model definition.
 * 
 * @author Rene Schoeffel
 */
public class Xmlparser {

	/**
	 * Return new initialized instance of {@link Model}.
	 * 
	 * @param Xmlfile
	 *            containing all species concentrations, reactions and
	 *            propensity functions, as well as tunable and constant rate
	 *            parameters
	 * @return new initialized instance of {@link Model}
	 */

	public Model generatemodel(File xmlfile) {

		Model m = new Model();
		Document doc = null;

		try {
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			doc = dBuilder.parse(xmlfile);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		NodeList nList = doc.getElementsByTagName("Species");
		for (int i = 0; i < nList.getLength(); i++) {
			Element e = (Element) nList.item(i);
			String tag = e.getAttribute("name");
			Double value = Double.parseDouble(e.getAttribute("value"));
			m.setSpecies(tag, value);
		}

		nList = doc.getElementsByTagName("Constant");
		for (int i = 0; i < nList.getLength(); i++) {
			Element e = (Element) nList.item(i);
			String tag = e.getAttribute("name");
			Double value = Double.parseDouble(e.getAttribute("value"));
			m.setConstant(tag, value);
		}

		nList = doc.getElementsByTagName("Tunable");
		for (int i = 0; i < nList.getLength(); i++) {
			Element e = (Element) nList.item(i);
			String tag = e.getAttribute("name");
			Double value = Double.parseDouble(e.getAttribute("value"));
			m.setTunable(tag, value);
		}

		nList = doc.getElementsByTagName("Propensity");
		for (int i = 0; i < nList.getLength(); i++) {
			Element e = (Element) nList.item(i);
			String tag = e.getAttribute("name");
			String value = e.getAttribute("expression");
			m.setPropensity(tag, value);
		}

		nList = doc.getElementsByTagName("Reaction");
		for (int i = 0; i < nList.getLength(); i++) {
			Element e = (Element) nList.item(i);
			String tag = e.getAttribute("name");
			HashMap<String, String> reactionmap = new HashMap<String, String>();
			NodeList subnList = e.getChildNodes();
			for (int j = 0; j < subnList.getLength(); j++) {
				if (subnList.item(j).getNodeType() == Node.ELEMENT_NODE) {
					Element f = (Element) subnList.item(j);
					String changedspecies = f.getAttribute("name");
					String changeexpression = f.getAttribute("expression");
					reactionmap.put(changedspecies, changeexpression);
				}
			}
			m.setReaction(tag, reactionmap);
		}
		m.pepareEvaluators();
		m.updateSpeciesVariables();
		m.updateConstantVariables();
		m.updateTunableVariables();
		m.updateDependency();
		return m;
	}
}
