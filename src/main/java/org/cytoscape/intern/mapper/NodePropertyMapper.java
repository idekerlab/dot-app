package org.cytoscape.intern.mapper;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.View;

import java.awt.Color;
import java.util.HashMap;


/**
 * Handles mapping of CyNode properties to .dot equivalent Strings
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NodePropertyMapper extends Mapper {
	
	/**
	 *  maps Cytoscape node shape types to the equivalent string used in .dot
	 */
	private HashMap<NodeShape, String> nodeShapeMap;
	
	/**
	 * Initializes and populates instance variables with mappings
	 * 
	 * @param view View of Node we are converting to .dot
	 */
	public NodePropertyMapper(View<CyNode> view) {
		super(view);
		// initialize hash maps
		simpleVisPropsToDot = new HashMap< VisualProperty<?>, String>();
		nodeShapeMap = new HashMap<NodeShape, String>();
		
		populateMaps();
	}
	
	/**
	 * Creates string for .dot style attribute. Appends border lineStyle and shape style (rounded or not etc.) to 
	 * "style = filled"
	 * 
	 * @return String for style attribute
	 */
	protected String mapDotStyle() {
		// TODO
		/**
		 * Pseudocode
		 * Call super.mapDotStyle(lineStyle) to retrieve 'style="lineStyle"' string
		 * Ignore final " (get first n-1 characters of string)
		 * Join with "filled" separated by ","
		 * determine if we need to add "rounded" by checking BVL.NODE_SHAPE
		 * concatenate if needed
		 * return created String
		 */
		StringBuilder dotStyle = new StringBuilder(super.mapDotStyle());
		NodeShape shape = view.getVisualProperty(BasicVisualLexicon.NODE_SHAPE);
		if (shape.equals(NodeShapeVisualProperty.ROUND_RECTANGLE)) {
			dotStyle.append("rounded,");
		}
		dotStyle.append("filled\"");
		return dotStyle.toString();
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		LOGGER.info("Populating HashMaps with values");

		//Put Simple Props Key/Values
		simpleVisPropsToDot.put(BasicVisualLexicon.NODE_LABEL, "label = \"" + 
			view.getVisualProperty(BasicVisualLexicon.NODE_LABEL) + "\"" );
		simpleVisPropsToDot.put(BasicVisualLexicon.NODE_BORDER_WIDTH, "penwidth = \"" +
			view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH) + "\"");
		simpleVisPropsToDot.put(BasicVisualLexicon.NODE_HEIGHT, "height = \"" +
			view.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT) + "\"");
		simpleVisPropsToDot.put(BasicVisualLexicon.NODE_WIDTH, "width = \"" +
			view.getVisualProperty(BasicVisualLexicon.NODE_WIDTH) + "\"");
		simpleVisPropsToDot.put(BasicVisualLexicon.NODE_TOOLTIP, "tooltip = \"" +
			view.getVisualProperty(BasicVisualLexicon.NODE_TOOLTIP) + "\"");
		
		//Put Node Shape Key/Values
		nodeShapeMap.put(NodeShapeVisualProperty.TRIANGLE, "triangle");
		nodeShapeMap.put(NodeShapeVisualProperty.DIAMOND, "diamond");
		nodeShapeMap.put(NodeShapeVisualProperty.ELLIPSE, "ellipse");
		nodeShapeMap.put(NodeShapeVisualProperty.HEXAGON, "hexagon");
		nodeShapeMap.put(NodeShapeVisualProperty.OCTAGON, "octagon");
		nodeShapeMap.put(NodeShapeVisualProperty.PARALLELOGRAM, "parallelogram");
		nodeShapeMap.put(NodeShapeVisualProperty.ROUND_RECTANGLE, "rectangle");
		nodeShapeMap.put(NodeShapeVisualProperty.RECTANGLE, "rectangle");
		LOGGER.info("HashMaps populated");
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		/**
		 * Pseudocode:
		 * elementString = ""
		 * For each prop in simpleVisPropsToDot do
		 * 		propVal = view.getVisualProperty(prop)
		 * 		elementString += mapVisToDot(prop, propVal)
		 * end
		 * 
		 * Get node border color and node border transparency values from view
		 * elementString += mapColor(nodeBorderColorVal, nodeBorderTransVal)
		 * 
		 * Get node fill color and node transparency (DOT ATTRIBUTE IS fillcolor)
		 * elementString += mapColor(nodeFillColor, nodeTransparency)
		 * 
		 * Get node label font face
		 * elementString += mapFont(nodeLabelFont)
		 * 
		 * return elementString
		 */
		LOGGER.info("Preparing to get .dot declaration for element.");

		//Build attribute string
		StringBuilder elementString = new StringBuilder("[");

		//Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot.values()) {
		        elementString.append(dotAttribute);
		        elementString.append(",");
		}
		LOGGER.info("Built up .dot string from simple properties. Resulting string: " + elementString);
		
		LOGGER.info("Preparing to get color properties");
		
		//Get the color string (border color). Append to attribute string
		Color borderColor = (Color) view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT);
		Integer borderTransparency = view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY);
		String dotBorderColor = String.format("color = \"%s\",", mapColorToDot(borderColor, borderTransparency));
		elementString.append(dotBorderColor);
		
		// Write node fill color
		Color fillColor = (Color) view.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);
		Integer nodeTransparency = view.getVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY);
		String dotFillColor = String.format("fillcolor = \"%s\",", mapColorToDot(fillColor, nodeTransparency));
		elementString.append(dotFillColor);
		LOGGER.info("Appended color attributes to .dot string. Result: " + elementString);
	
		LOGGER.info("Preparing to get shape property");
		//Get the .dot string for the node shape. Append to attribute string
		NodeShape shape = view.getVisualProperty(BasicVisualLexicon.NODE_SHAPE);
		String shapeStr = nodeShapeMap.get(shape);
		if (shapeStr == null) {
			shapeStr = "rectangle"; 
			LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
		}
		String dotShape = String.format("shape = \"%s\"", shapeStr);
		elementString.append(dotShape);
		LOGGER.info("Appended shape attribute to .dot string. Result: " + elementString);
		
		elementString.append(",");
		
		//Get the .dot string for the node style. Append to attribute string
		elementString.append(mapDotStyle());
		
		elementString.append(",");
		
		//Finish attribute string with mandatory fixedsize = true attribute
		elementString.append("fixedsize = true]");
		LOGGER.info("Created .dot string. Result: " + elementString);
		return elementString.toString();
	} 
}
