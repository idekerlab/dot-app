package org.cytoscape.intern;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.EdgeBendVisualProperty;

import java.util.HashMap;

/**
 * This class contains the mappings of edge VisualPropertys to their equivalent
 * dot attribute strings and performs the translations of Cytoscape edges to
 * dot edge declarations
 * @author Braxton Fitts
 * @author Massoud Maher
 * @author Ziran Zhang 
 * 
 */
public class EdgePropertyMapper extends Mapper {
	/**
	 * Maps Cytoscape VisualProperty types by their String ID to their dot equivalent
	 */
	private HashMap<String, String> simpleVisToDot; // TODO fill in
	/**
	 * Maps Cytoscape VisualProperty types by their String ID to a HashMap that
	 * contains the Cytoscape to *.dot mappings for that type
	 */
	private HashMap<String, String> discreteMappingTypes; // TODO fill in
	/**
	 * Maps Cytoscape arrowhead types to the equivalent dot attribute
	 */
	private HashMap<ArrowShapeVisualProperty, String> arrowShapeMap; // TODO fill in
	
	/**
	 * Translates the Cytoscape Source Arrow Shape property of the View<CyEdge>
	 * to the equivalent dot attribute string
	 * @param edgeView The edge view whose property is to be translated
	 * @return The dot attribute string which sets the source arrow shape
	 */
	public String setSourceArrowShape(View<CyEdge> edgeView){
		// TODO
		return null;
	}
	
	/**
	 * Translates the Cytoscape Target Arrow Shape property of the View<CyEdge>
	 * to the equivalent dot attribute string
	 * @param edgeView The edge view whose property is to be translated
	 * @return The dot attribute string which sets the target arrow shape
	 */
	public String setTargetArrowShape(View<CyEdge> edgeView){
		// TODO
		return null;
	}
	
	/**
	 * Translates the Cytoscape Bend property of the View<CyEdge> object to the
	 * equivalent dot attribute string
	 * @param edgeView The edge view whose property is to be translated
	 * @return
	 */
	public String mapEdgeBend(View<CyEdge> edgeView){
		// TODO
		return null;
	}
}