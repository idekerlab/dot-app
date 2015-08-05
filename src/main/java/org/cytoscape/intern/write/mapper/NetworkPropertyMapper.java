package org.cytoscape.intern.write.mapper;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import static org.cytoscape.view.presentation.property.ArrowShapeVisualProperty.*;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.*;

import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualStyle;

public class NetworkPropertyMapper extends Mapper {

	private boolean directed = false;
	
	// Value of splines attribute
	private String splinesVal;
	
	// Location of graph label, null if no graph label is desired
	private String labelLoc;

	/**
	 * Constructs NetworkPropertyMapper object
	 * @param vizStyle 
	 * 
	 * @param view of network we are converting
	 */
	public NetworkPropertyMapper(CyNetworkView netView, boolean directed, String splinesVal, String labelLoc, VisualStyle vizStyle) {
		super(netView, vizStyle);
		simpleVisPropsToDot = new ArrayList<String>();
		this.directed = directed;
		this.splinesVal = splinesVal;
		this.labelLoc = labelLoc;
		this.vizStyle = vizStyle;
		
		populateMaps();		
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		// Get network name from model. Remove spaces from name
		CyNetwork network = (CyNetwork)view.getModel();
		String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		// remove dis-allowed characters to avoid errors
		// filter out disallowed chars
		networkName = Mapper.modifyElementId(networkName);

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
		
		elementString.append(
			getNodeDefaults() + "\n"
		);
		elementString.append(
			getEdgeDefaults() + "\n"
		);
		
		return elementString.toString();
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		// if graph label is desired
		if(labelLoc != null) {
			// label attribute of graph
			String label = view.getVisualProperty(NETWORK_TITLE);
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

	/**
	 * Returns dot string that represents if graph is directed or not
	 * 
	 * @return String that is either "graph" or "digraph"
	 */
	private String getDirectedString() {
		String output = (directed) ? "digraph":"graph";
		return output;
	}
	
	private String getNodeDefaults() {
		StringBuilder nodeDefaults = new StringBuilder("node [");
		
		//Node SimpleVizProps
		Double borderWidth = vizStyle.getDefaultValue(NODE_BORDER_WIDTH);
		nodeDefaults.append(
			String.format("penwidth = \"%f\"", borderWidth) + ","
		);
		
		Double height = vizStyle.getDefaultValue(NODE_HEIGHT);
		nodeDefaults.append(
			String.format("height = \"%f\"", height/PPI) + ","
		);

		Double width = vizStyle.getDefaultValue(NODE_WIDTH);
		nodeDefaults.append(
			String.format("width = \"%f\"", width/PPI) + ","
		);

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
			LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
		}
		
		String dotShape = String.format("shape = \"%s\"", shapeStr);
		nodeDefaults.append(dotShape + ",");
		
		nodeDefaults.append(
			mapDefaultNodeDotStyle(shape) +","
		);


		Font fontName = vizStyle.getDefaultValue(NODE_LABEL_FONT_FACE);
		LOGGER.info("Retrieving font size...");
		Integer fontSize = ((Number)vizStyle.getDefaultValue(NODE_LABEL_FONT_SIZE)).intValue();
		Color fontColor = (Color)(vizStyle.getDefaultValue(NODE_LABEL_COLOR));
		Integer fontTransparency = ((Number)vizStyle.getDefaultValue(NODE_LABEL_TRANSPARENCY)).intValue();
		
		String fontString = mapDefaultFont(fontName, fontSize, fontColor, fontTransparency);
		nodeDefaults.append(fontString + ",");
		
		nodeDefaults.append(
			"fixedsize = \"true\",labelloc = \"" + labelLoc + "\"]"
		);
		return nodeDefaults.toString();
	}
	
	private String getEdgeDefaults() {
		StringBuilder edgeDefaults = new StringBuilder("edge [");
		Double width = vizStyle.getDefaultValue(EDGE_WIDTH);
		edgeDefaults.append(String.format("penwidth = \"%f\"", width) + ",");

		String tooltip = vizStyle.getDefaultValue(EDGE_TOOLTIP);
		edgeDefaults.append(String.format("tooltip = \"%s\"", tooltip) + ",");
		
		// block is non-functioning. only works for bypasses due to what we think is source error
		ArrowShape targetArrow = vizStyle.getDefaultValue(EDGE_TARGET_ARROW_SHAPE);
		LOGGER.info("Retrieving Default target/head arrow. CS version is: " + targetArrow);
		String dotTargetArrow = ARROW_SHAPE_MAP.get(targetArrow);
		LOGGER.info("Target/head arrow retrieved. .dot verison is: " + dotTargetArrow);
		edgeDefaults.append(String.format("arrowhead = \"%s\"", dotTargetArrow) + ",");
			
		ArrowShape sourceArrow = vizStyle.getDefaultValue(EDGE_SOURCE_ARROW_SHAPE);
		LOGGER.info("Retrieving Default source/tail arrow. CS version is: " + sourceArrow);
		String dotSourceArrow = ARROW_SHAPE_MAP.get(sourceArrow);
		LOGGER.info("Source/tail arrow retrieved. .dot verison is: " + dotSourceArrow);
		edgeDefaults.append(String.format("arrowtail = \"%s\"", dotSourceArrow) + ",");
		
		Color strokeColor = (Color) vizStyle.getDefaultValue(EDGE_STROKE_UNSELECTED_PAINT);
		Integer strokeTransparency = ((Number)vizStyle.getDefaultValue(EDGE_TRANSPARENCY)).intValue();
		String dotColor = String.format("color = \"%s\"", mapColorToDot(strokeColor, strokeTransparency));
		edgeDefaults.append(dotColor + ",");

		// Get label font information and append in proper format
		Color labelColor = (Color) vizStyle.getDefaultValue(EDGE_LABEL_COLOR);
		// Set alpha (opacity) to 0 if node is invisible, translate alpha otherwise
		Integer labelTransparency = ((Number)vizStyle.getDefaultValue(EDGE_LABEL_TRANSPARENCY)).intValue();
		Font labelFont = vizStyle.getDefaultValue(EDGE_LABEL_FONT_FACE);
		Integer labelSize = ((Number)vizStyle.getDefaultValue(EDGE_LABEL_FONT_SIZE)).intValue();
		String fontString = mapDefaultFont(labelFont, labelSize, labelColor, labelTransparency);
		edgeDefaults.append(fontString + ",");
		
		LOGGER.info("Appending Default style attribute to .dot string");
		String styleString = mapDefaultEdgeDotStyle();
		edgeDefaults.append(styleString + ",");
		edgeDefaults.append("dir = \"both\"]");
		return edgeDefaults.toString();
	}
	
	private String mapDefaultFont(Font font, Integer size, Color color, Integer transparency) {
		
		LOGGER.info("Label font, size, color, and transparency translation");
		 
		String returnValue = "";
		
		LOGGER.info("Label font, size, color, and transparency translation");
		 
		returnValue += "fontname = \"" + font.getFontName() + "\",";  
		returnValue += "fontsize = \"" + size.toString() + "\","; 
		returnValue += "fontcolor = \"" + mapColorToDot(color, transparency) + "\"";

		LOGGER.info("Dot attributes associate with font is: " + returnValue);
			
		
		return returnValue;
		
	}
	
	private String mapDefaultNodeDotStyle(NodeShape shape) {
		//NODE_BORDER_LINE_TYPE is field in org.cytoscape.view.presentation.property.BasicVisualLexicon
		LineType lineType = vizStyle.getDefaultValue(NODE_BORDER_LINE_TYPE);
		// get .dot equivalent of line style
		String lineStr = LINE_TYPE_MAP.get(lineType);
		if (lineStr == null) {
			lineStr = "solid";
			LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
		}
		String shapeString = (shape.equals(ROUND_RECTANGLE)) ? "rounded," : "";
		String style = String.format("style = \"%s,%sfilled\"", lineStr, shapeString);

		return style;
	}
	private String mapDefaultEdgeDotStyle() {
		//EDGE_LINE_TYPE is field in org.cytoscape.view.presentation.property.BasicVisualLexicon
		LineType lineType = vizStyle.getDefaultValue(EDGE_LINE_TYPE);
		String lineStr = LINE_TYPE_MAP.get(lineType);
		if (lineStr == null) {
			lineStr = "solid";
			LOGGER.warning("Visual Style default EDGE_LINE_TYPE doesn't map to a .dot attribute. Setting to default");
		}
		String style = String.format("style = \"%s\"", lineStr);
		return style;
	}
	
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
}
