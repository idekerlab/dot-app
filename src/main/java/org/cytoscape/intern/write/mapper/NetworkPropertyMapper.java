/**************************
 * Copyright © 2015-2017 Braxton Fitts, Ziran Zhang, Massoud Maher
 * 
 * This file is part of dot-app.
 * dot-app is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * dot-app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with dot-app.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cytoscape.intern.write.mapper;

import static org.cytoscape.view.presentation.property.ArrowShapeVisualProperty.NONE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TOOLTIP;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TOOLTIP;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.ROUND_RECTANGLE;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualStyle;

public class NetworkPropertyMapper extends Mapper {

	/**
	 * Determines whether the graph is visibly directed or not
	 * 
	 * @param networkView The network being tested for directedness
	 * @return true if graph is directed, false otherwise
	 */
	public static boolean isDirected(CyNetworkView networkView) {
		// get all the edge views from the current networkview
		Collection<View<CyEdge>> edgeViews = networkView.getEdgeViews();
		
		/**
		 * iterate each edgeview to check whether there is a target arrow shape
		 * or a source arrow shape for that edge
		 */
		ArrowShape noArrow = NONE;
        for (View<CyEdge> edge: edgeViews){
        	ArrowShape sourceArrow = edge.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE);
        	ArrowShape targetArrow = edge.getVisualProperty(EDGE_TARGET_ARROW_SHAPE);
        	if(!targetArrow.equals(noArrow) || !sourceArrow.equals(noArrow)) {
        		//return true if there is at least one not NONE arrow shape
        		return true;
        	}
        }
        
        // return false if the graph is undirected (has only NONE arrow shapes)
        return false;
	}
	
	private boolean directed = false;
	
	// Value of splines attribute
	private String splinesVal;
	
	// Location of graph label, null if no graph label is desired
	private String labelLoc;

	// Location of node label
	private String nodeLabelLoc;
	
	/**
	 * Constructs NetworkPropertyMapper object
	 * @param netView view being mapped
	 * @param directed records whether network is directed
	 * @param splinesVal how edges should be drawn by GraphViz programs
	 * @param labelLoc label location of graph label (if shown)
	 * @param nodeLabelLoc label location of node labels
	 * @param vizStyle VisualStyle applied to view
	 * 
	 */
	public NetworkPropertyMapper(CyNetworkView netView, boolean directed, String splinesVal, String labelLoc, String nodeLabelLoc, VisualStyle vizStyle) {
		super(netView, vizStyle);
		simpleVisPropsToDot = new ArrayList<String>();
		this.directed = directed;
		this.splinesVal = splinesVal;
		this.labelLoc = labelLoc;
		this.nodeLabelLoc = nodeLabelLoc;
		this.vizStyle = vizStyle;
		
		populateMaps();		
	}
	
	/**
	 * Returns dot string that represents if graph is directed or not
	 * 
	 * @return String that is either "graph" or "digraph"
	 */
	private String getDirectedString() {
		String output = (directed) ? "digraph":"graph";
		return output;
	}

	/**
	 * Returns a string that has the edge[attrs] declaration
	 * defines default attribute values for edges
	 * 
	 * @return String in form edges[attrs=..] that sets the default
	 * value for attributes
	 */
	private String getEdgeDefaults() {
		LOGGER.info("Building edge default string...");
		StringBuilder edgeDefaults = new StringBuilder("edge [");

		LOGGER.trace("Appending label attr to default string...");
		String edgeLabel = vizStyle.getDefaultValue(EDGE_LABEL);
		edgeLabel = edgeLabel.replace("\\", "\\\\");
		edgeLabel = edgeLabel.replace("\"", "\\\"");
		edgeDefaults.append(
			String.format("label = \"%s\"", edgeLabel) + ","
		);

		LOGGER.trace("Appending penwidth attr to default string...");
		Double width = vizStyle.getDefaultValue(EDGE_WIDTH);
		edgeDefaults.append(String.format("penwidth = \"%s\"", decimalFormatter.format(width)) + ",");

		LOGGER.trace("Appending tooltip attr to default string...");
		String tooltip = vizStyle.getDefaultValue(EDGE_TOOLTIP);
		edgeDefaults.append(String.format("tooltip = \"%s\"", tooltip) + ",");
		
		// block is non-functioning. only works for bypasses due to what we think is source error
		LOGGER.trace("Appending arrowhead attr to default string...");
		ArrowShape targetArrow = vizStyle.getDefaultValue(EDGE_TARGET_ARROW_SHAPE);
		LOGGER.trace("CS target/head arrow: " + targetArrow);
		String dotTargetArrow = ARROW_SHAPE_MAP.get(targetArrow);
		LOGGER.trace(".dot Target/head arrow: " + dotTargetArrow);
		edgeDefaults.append(String.format("arrowhead = \"%s\"", dotTargetArrow) + ",");
			
		LOGGER.trace("Appending arrowtail attr to default string...");
		ArrowShape sourceArrow = vizStyle.getDefaultValue(EDGE_SOURCE_ARROW_SHAPE);
		LOGGER.trace("CS source/tail arrow: " + sourceArrow);
		String dotSourceArrow = ARROW_SHAPE_MAP.get(sourceArrow);
		LOGGER.trace(".dot source/tail arrow: " + dotSourceArrow);
		edgeDefaults.append(String.format("arrowtail = \"%s\"", dotSourceArrow) + ",");
		
		LOGGER.trace("Appending color attr to default string...");
		Color strokeColor = (Color) vizStyle.getDefaultValue(EDGE_STROKE_UNSELECTED_PAINT);
		Integer strokeTransparency = ((Number)vizStyle.getDefaultValue(EDGE_TRANSPARENCY)).intValue();
		String dotColor = String.format("color = \"%s\"", mapColorToDot(strokeColor, strokeTransparency));
		edgeDefaults.append(dotColor + ",");

		LOGGER.trace("Appending fontname, fontsize, and fontcolor attrs"
				+ " to default string...");
		// Get label font information and append in proper format
		Color labelColor = (Color) vizStyle.getDefaultValue(EDGE_LABEL_COLOR);
		// Set alpha (opacity) to 0 if node is invisible, translate alpha otherwise
		Integer labelTransparency = ((Number)vizStyle.getDefaultValue(EDGE_LABEL_TRANSPARENCY)).intValue();
		Font labelFont = vizStyle.getDefaultValue(EDGE_LABEL_FONT_FACE);
		Integer labelSize = ((Number)vizStyle.getDefaultValue(EDGE_LABEL_FONT_SIZE)).intValue();
		String fontString = mapDefaultFont(labelFont, labelSize, labelColor, labelTransparency);
		edgeDefaults.append(fontString + ",");
		
		LOGGER.trace("Appending Default style attribute to .dot string");
		String styleString = mapDefaultEdgeDotStyle();
		edgeDefaults.append(styleString + ",");
		edgeDefaults.append("dir = \"both\"]");
		return edgeDefaults.toString();
	}
	
	/**
	 * Returns a string that has the node[attrs] declaration
	 * defines default attribute values for nodes
	 * 
	 * @return String in form node[attrs=..] that sets the default
	 * value for attributes
	 */
	private String getNodeDefaults() {
		LOGGER.info("Building node default string...");
		StringBuilder nodeDefaults = new StringBuilder("node [");
		
		//Node SimpleVizProps
		LOGGER.trace("Appending label attr to default string...");
		String nodeLabel = vizStyle.getDefaultValue(NODE_LABEL);
		nodeLabel = nodeLabel.replace("\\", "\\\\");
		nodeLabel = nodeLabel.replace("\"", "\\\"");
		if(!nodeLabelLoc.equals("ex")) {
			nodeDefaults.append(
				String.format("label = \"%s\"", nodeLabel) + ","
			);
		}
		// if external label
		else {
			nodeDefaults.append("label = \"\"");
			nodeDefaults.append(
				String.format("xlabel = \"%s\"", nodeLabel)
			);
		}
		
		LOGGER.trace("Appending penwidth attr to default string...");
		Double borderWidth = vizStyle.getDefaultValue(NODE_BORDER_WIDTH);
		nodeDefaults.append(
			String.format("penwidth = \"%s\"", decimalFormatter.format(borderWidth)) + ","
		);
	
		// set width and height, if they are locked, must set to NODE_SIZE prop
		Double height, width;
		LOGGER.debug("ISLOCKED: " + nodeSizesLocked);
		if(nodeSizesLocked) {
			height = vizStyle.getDefaultValue(NODE_SIZE);
			width = vizStyle.getDefaultValue(NODE_SIZE);
		}
		else {
			height = vizStyle.getDefaultValue(NODE_HEIGHT);
			width = vizStyle.getDefaultValue(NODE_WIDTH);
		}
		height /=PPI;
		width /=PPI;
		
		nodeDefaults.append(
			String.format("height = \"%s\"", decimalFormatter.format(height)) + ","
		);

		nodeDefaults.append(
			String.format("width = \"%s\"", decimalFormatter.format(width)) + ","
		);

		// set tooltip
		String tooltip = vizStyle.getDefaultValue(NODE_TOOLTIP);
		nodeDefaults.append(
			String.format("tooltip = \"%s\"", tooltip) + ","
		);

		// Get the color string (border color). Append to attribute string
		Color borderColor = (Color) vizStyle.getDefaultValue(NODE_BORDER_PAINT);
		// Set alpha (opacity) to 0 if node is invisible, translate alpha otherwise
		Integer borderTransparency = ((Number)vizStyle.getDefaultValue(NODE_BORDER_TRANSPARENCY)).intValue();
		String dotBorderColor = String.format("color = \"%s\"", mapColorToDot(borderColor, borderTransparency));
		nodeDefaults.append(dotBorderColor + ",");
		
		// Write node fill color
		Color fillColor = (Color) vizStyle.getDefaultValue(NODE_FILL_COLOR);
		Integer nodeTransparency = ((Number)vizStyle.getDefaultValue(NODE_TRANSPARENCY)).intValue();
		String dotFillColor = String.format("fillcolor = \"%s\"", mapColorToDot(fillColor, nodeTransparency));
		nodeDefaults.append(dotFillColor + ",");

		// Get the .dot string for the node shape. Append to attribute string
		NodeShape shape = vizStyle.getDefaultValue(NODE_SHAPE);
		String shapeStr = NODE_SHAPE_MAP.get(shape);
		
		// default if there is no match
		if (shapeStr == null) {
			shapeStr = "rectangle"; 
			LOGGER.warn("Cytoscape property doesn't map to a .dot attribute. Setting to default");
		}
		
		String dotShape = String.format("shape = \"%s\"", shapeStr);
		nodeDefaults.append(dotShape + ",");
		
		nodeDefaults.append(
			mapDefaultNodeDotStyle(shape) +","
		);


		Font fontName = vizStyle.getDefaultValue(NODE_LABEL_FONT_FACE);
		LOGGER.trace("Retrieving font size...");
		Integer fontSize = ((Number)vizStyle.getDefaultValue(NODE_LABEL_FONT_SIZE)).intValue();
		Color fontColor = (Color)(vizStyle.getDefaultValue(NODE_LABEL_COLOR));
		Integer fontTransparency = ((Number)vizStyle.getDefaultValue(NODE_LABEL_TRANSPARENCY)).intValue();
		
		String fontString = mapDefaultFont(fontName, fontSize, fontColor, fontTransparency);
		nodeDefaults.append(fontString + ",");
		
		nodeDefaults.append(
			"fixedsize = \"true\",labelloc = \"" + nodeLabelLoc + "\"]"
		);
		
		String result = nodeDefaults.toString();
		LOGGER.debug("Built node default string: " + result);
		return result;
	}
	
	/**
	 * Returns "style" attribute intended for default edge declaration
	 * 
	 * @return String in form "style=..."
	 */
	private String mapDefaultEdgeDotStyle() {
		LineType lineType = vizStyle.getDefaultValue(EDGE_LINE_TYPE);
		Boolean isVisible = vizStyle.getDefaultValue(EDGE_VISIBLE);
		String lineStr = LINE_TYPE_MAP.get(lineType);
		if (lineStr == null) {
			lineStr = "solid";
			LOGGER.warn("Visual Style default EDGE_LINE_TYPE doesn't map to a .dot attribute. Setting to default");
		}
		String invisString = (!isVisible) ? ",invis" : "";
		String style = String.format("style = \"%s%s\"", lineStr, invisString);
		return style;
	}

	/**
	 * Returns String for default Fonts
	 * 
	 * @param font font face
	 * @param size size of font
	 * @param color color
	 * @param transparency alpha value. 0-255
	 * @return String in form "fontname=...,fontsize=...,fontcolor=..."
	 */
	private String mapDefaultFont(Font font, Integer size, Color color, Integer transparency) {
		
		LOGGER.trace("Label font, size, color, and transparency translation");
		 
		String returnValue = "";
		
		returnValue += "fontname = \"" + font.getFontName() + "\",";  
		returnValue += "fontsize = \"" + size.toString() + "\","; 
		returnValue += "fontcolor = \"" + mapColorToDot(color, transparency) + "\"";

		LOGGER.debug("Dot attributes associate with font is: " + returnValue);
			
		return returnValue;
	}

	/**
	 * Returns "style" attribute intended for default node declaration
	 * 
	 * @param shape NodeShape default value
	 * @return String in form "style=...,filled"
	 */
	private String mapDefaultNodeDotStyle(NodeShape shape) {
		LineType lineType = vizStyle.getDefaultValue(NODE_BORDER_LINE_TYPE);
		Boolean isVisible = vizStyle.getDefaultValue(NODE_VISIBLE);
		// get .dot equivalent of line style
		String lineStr = LINE_TYPE_MAP.get(lineType);
		if (lineStr == null) {
			lineStr = "solid";
			LOGGER.warn("Cytoscape property doesn't map to a .dot attribute. Setting to default");
		}
		String shapeString = (shape.equals(ROUND_RECTANGLE)) ? "rounded," : "";
		String invisString = (!isVisible) ? "invis," : "";
		String style = String.format("style = \"%s,%s%sfilled\"", lineStr, shapeString, invisString);

		return style;
	}

	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		// if graph label is desired
		if(labelLoc != null) {
			// label attribute of graph
			String label = view.getVisualProperty(NETWORK_TITLE);
			label = label.replace("\\", "\\\\");
			label = label.replace("\"", "\\\"");
			String dotLabel = String.format("label = \"%s\"", label);
			simpleVisPropsToDot.add(dotLabel);
			
			// desired label location
			simpleVisPropsToDot.add("labelloc = " + labelLoc);
		}

		// Background color of graph
		Color netBgColor = (Color)view.getVisualProperty(NETWORK_BACKGROUND_PAINT);
		String dotBgColor = String.format("bgcolor = \"%s\"", mapColorToDot(netBgColor, netBgColor.getAlpha()));
		simpleVisPropsToDot.add(dotBgColor);
		
		// splines value
		simpleVisPropsToDot.add(String.format("splines = \"%s\"", splinesVal));
		
		// output order
		simpleVisPropsToDot.add("outputorder = \"edgesfirst\"");
		
		// esep=0 so splines can always be routed around nodes
		simpleVisPropsToDot.add("esep = \"0\"");
		
		// pad so (ideally) no labels are cut off
		simpleVisPropsToDot.add("pad = \"2\"");
	}
	
	@Override
	protected String mapDotStyle() {
		// NOT USED
		return null;
	}

	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		// Get network name from model. Remove spaces from name
		CyNetwork network = (CyNetwork)view.getModel();
		String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		// filter out disallowed chars
		networkName = Mapper.modifyElementID(networkName);

		// Build the network properties string
		StringBuilder elementString = new StringBuilder();
		
		// Header of the dot file of the form (di)graph [NetworkName] {
		String graphDeclaration = String.format("%s %s {", getDirectedString(), networkName);
		elementString.append(graphDeclaration + "\n");
		
		//added outputorder = edgesfirst at the beginning of the file to make sure all the nodes 
		//are on the top of the edges.
		
		// Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot) {
		        elementString.append(dotAttribute + "\n");
		}
		
		elementString.append(getNodeDefaults() + "\n");
		elementString.append(getEdgeDefaults() + "\n");
		
		LOGGER.debug(
			String.format("Built graph header: %s", elementString.toString())
		);
		
		return elementString.toString();
	}
}
