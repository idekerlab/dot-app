package org.cytoscape.intern.mapper;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
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
	private HashMap<VisualProperty, String> simpleVisPropsToDot;
	
	/**
	 * Maps Cytoscape arrowhead types to the equivalent dot attribute
	 */
	private HashMap<ArrowShape, String> arrowShapeMap; // TODO fill in
	
	
	public EdgePropertyMapper(View<?> view) {
		super(view);
		
		simpleVisPropsToDot = new HashMap< VisualProperty, String>();
		arrowShapeMap = new HashMap<ArrowShape, String>();
		
		populateMaps();
		
	}
	
	/**
	 * 
	 * @return Strings that define arrow shape attributes
	 */
	private String setArrowShapes() {
		/**
		 * pseudocode
		 * 
		 * get Strings from hashMap and concatenate to eachother with "arrowshape="
		 */
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
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		//TODO
		/**
		 * Pseudocode:
		 * elementString = ""
		 * For each BasicVisualLexiconProperty prop do
		 * 		propVal = edgeView.getVisualProperty(prop)
		 * 		elementString += edgeMapper.mapVisToDot(prop, propVal)
		 * end
		 * 
		 * Get stroke color and edge transparency values from view
		 * elementString += edgeMapper.mapColor(strokeColorVal, edgeTransVal)
		 * 
		 * Get Source Arrowhead color (DOT ATTRIBUTE IS fillcolor, NO TRANSPARENCY)
		 * elementString += edgeMapper.mapColor(sourceArrowColor, 255)
		 * 
		 * Get Target Arrowhead color (DOT ATTRIBUTE IS fillcolor, NO TRANSPARENCY)
		 * elementString += edgeMapper.mapColor(targetArrowColor, 255)
		 * 
		 * Get Source Arrowhead Shape
		 * elementString += edgeMapper.setSourceArrowShapes()
		 * 
		 * Get Edge Label Font Face
		 * elementString += edgeMapper.mapFont(edgeLabelFont)
		 * 
		 * set line style
		 * 
		 * return elementString
		 */
		return null;
	}
}