package org.cytoscape.intern;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.CyNetworkView;

import java.awt.Color
import java.awt.GraphicsEnvironment

/**
 * An instance of manager, which is constructed in 
 * DotWriteTask class. Calls methods which return a 
 * property String based on the passed in View.  
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DataManager {
  
	//instance of NodePropertyMapper class
	NodePropertyMapper nodeMapper;

	//instance of EdgePropertyMapper class
	EdgePropertyMapper edgeMapper;

  
	/**
	 * checks out which method,either getEdgeString or getNodeString, 
	 * will be called based on the parameter 
	 *
	 * @param pass in the View (Edge's or Node's), 
	 * which determines Edge property or Node property
	 */
	public String getElementString (View<Object> elementView) {
		//TODO
		/**
		 * Pseudocode:
		 * If elementView instanceof View<CyEdge> then
		 * 		call getEdgeString(elementView)
		 * Elseif elementView instanceof View<CyNode> then
		 * 		call getNodeString(elementView)
		 */
		return null;
	}
 
	/**
	 * Calls method mapVisToDot in EdgePropertyManager class 
	 *
	 * @param pass in the Edge View
	 */
	public String getEdgeString(View<CyEdge> edgeView) {
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
		 * elementString += edgeMapper.setSourceArrowShape()
		 * 
		 * Get Target Arrowhead Shape
		 * elementString += edgeMapper.setTargetArrowShape()
		 * 
		 * Get Edge Label Font Face
		 * elementString += edgeMapper.mapFont(edgeLabelFont)
		 * 
		 * return elementString
		 */
		return null;
	}

	/**
	 * Calls method mapVisToDot in NodePropertyManager class 
	 *
	 * @param pass in the Node View
	 */
	public String getNodeString(View<CyNode> nodeView) {
		//TODO

		/**
		 * Pseudocode:
		 * elementString = ""
		 * For each BasicVisualLexiconProperty prop do
		 * 		propVal = nodeView.getVisualProperty(prop)
		 * 		elementString += nodeMapper.mapVisToDot(prop, propVal)
		 * end
		 * 
		 * Get node border color and node border transparency values from view
		 * elementString += nodeMapper.mapColor(strokeColorVal, edgeTransVal)
		 * 
		 * Get node fill color and node transparency (DOT ATTRIBUTE IS fillcolor)
		 * elementString += nodeMapper.mapColor(sourceArrowColor, nodeTransparency)
		 * 
		 * Get node label font face
		 * elementString += nodeMapper.mapFont(edgeLabelFont)
		 * 
		 * return elementString
		 */
		return null;
	}

}
