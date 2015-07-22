package org.cytoscape.intern.mapper;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.model.View;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
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
	private static final HashMap<NodeShape, String> NODE_SHAPE_MAP = new HashMap<NodeShape, String>();
	static {
		NODE_SHAPE_MAP.put(NodeShapeVisualProperty.TRIANGLE, "triangle");
		NODE_SHAPE_MAP.put(NodeShapeVisualProperty.DIAMOND, "diamond");
		NODE_SHAPE_MAP.put(NodeShapeVisualProperty.ELLIPSE, "ellipse");
		NODE_SHAPE_MAP.put(NodeShapeVisualProperty.HEXAGON, "hexagon");
		NODE_SHAPE_MAP.put(NodeShapeVisualProperty.OCTAGON, "octagon");
		NODE_SHAPE_MAP.put(NodeShapeVisualProperty.PARALLELOGRAM, "parallelogram");
		NODE_SHAPE_MAP.put(NodeShapeVisualProperty.ROUND_RECTANGLE, "rectangle");
		NODE_SHAPE_MAP.put(NodeShapeVisualProperty.RECTANGLE, "rectangle");
	}
	
	/**
	 * Initializes and populates instance variables with mappings
	 * 
	 * @param view View of Node we are converting to .dot
	 */
	public NodePropertyMapper(View<CyNode> view) {
		super(view);
		// initialize data structure
		simpleVisPropsToDot = new ArrayList<String>();
		
		populateMaps();
	}
	
	/**
	 * Creates string for .dot style attribute. Appends border lineStyle and shape style (rounded or not etc.) to 
	 * "style = filled"
	 * 
	 * @return String for style attribute
	 */
	protected String mapDotStyle() {
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

		// Put Simple Props Key/Values
		String nodeLabel = view.getVisualProperty(BasicVisualLexicon.NODE_LABEL);
		// remove quotes
		nodeLabel.replace("\"", "");
		simpleVisPropsToDot.add(String.format("label = \"%s\"", nodeLabel));
		
		Double borderWidth = view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH);
		simpleVisPropsToDot.add(String.format("penwidth = \"%f\"", borderWidth));
		
		Double height = view.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
		simpleVisPropsToDot.add(String.format("height = \"%f\"", height/PPI));

		Double width = view.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
		simpleVisPropsToDot.add(String.format("width = \"%f\"", width/PPI));

		String tooltip = view.getVisualProperty(BasicVisualLexicon.NODE_TOOLTIP);
		simpleVisPropsToDot.add(String.format("tooltip = \"%s\"", tooltip));
		
		// Put Node Shape Key/Values
		LOGGER.info("HashMaps populated");
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		LOGGER.info("Preparing to get .dot declaration for a node.");

		// Build attribute string
		StringBuilder elementString = new StringBuilder("[");

		// Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot) {
		        elementString.append(dotAttribute);
		        elementString.append(",");
		}
		LOGGER.info("Built up .dot string from simple properties. Resulting string: " + elementString);
		
		// Write fillcolor and color attribute
		elementString.append(mapColors() + ",");
		LOGGER.info("Appended color attributes to .dot string. Result: " + elementString);

		// Write nodeShape
		elementString.append(mapShape() + ",");
		LOGGER.info("Appended shape attribute to .dot string. Result: " + elementString);
		

		// Get the .dot string for the node style. Append to attribute string
		elementString.append(mapDotStyle() + ",");
		LOGGER.info("Font data appended. Resulting String: " + elementString);
		
		// Get node location and append in proper format
		Double xLoc = view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		Double yLoc = view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		String dotPosition = String.format("pos = \"%s\"", mapPosition(xLoc, yLoc));
		elementString.append(dotPosition + ",");
		


		// Append font name+size+color attributes
		LOGGER.info("Appending font data");
		elementString.append(mapFontHelper() + ",");

		
		// Finish attribute string with mandatory fixedsize = true attribute
		elementString.append("fixedsize = true]");
		LOGGER.info("Created .dot string. Result: " + elementString);
		return elementString.toString();
	}
	
	/**
	 * Helper method that returns String that defines color attribute including "fillcolor=" part
	 * 
	 * @return String in form "color = <color>,fillcolor = <color>"
	 */
	private String mapColors() {
		StringBuilder elementString = new StringBuilder();
		
		LOGGER.info("Preparing to get color properties");
		// Get the color string (border color). Append to attribute string
		Color borderColor = (Color) view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT);
		Integer borderTransparency = view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY);
		String dotBorderColor = String.format("color = \"%s\"", mapColorToDot(borderColor, borderTransparency));
		elementString.append(dotBorderColor + ",");
		
		// Write node fill color
		Color fillColor = (Color) view.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);
		Integer nodeTransparency = view.getVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY);
		String dotFillColor = String.format("fillcolor = \"%s\"", mapColorToDot(fillColor, nodeTransparency));
		elementString.append(dotFillColor);
		
		return elementString.toString();
	}
	
	/**
	 * Helper method that returns String that represents nodeShape 
	 * 
	 * @return String in form in form "shape = <shape>"
	 */
	private String mapShape() {
		LOGGER.info("Preparing to get shape property");
		StringBuilder elementString = new StringBuilder();
		
		// Get the .dot string for the node shape. Append to attribute string
		NodeShape shape = view.getVisualProperty(BasicVisualLexicon.NODE_SHAPE);
		String shapeStr = NODE_SHAPE_MAP.get(shape);
		
		// default if there is no match
		if (shapeStr == null) {
			shapeStr = "rectangle"; 
			LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
		}
		
		String dotShape = String.format("shape = \"%s\"", shapeStr);
		elementString.append(dotShape);
		LOGGER.info("Appended shape attribute to .dot string. Result: " + elementString);
		
		return elementString.toString();
	}
	
	/**
	 * Helper method that returns String that contains font face, size, color and transparency
	 * 
	 * @return String that defines fontname, fontcolor and fontsize attributes
	 */
	private String mapFontHelper() {
		Font fontName = view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_FACE);
		Integer fontSize = view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		Color fontColor = (Color)(view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR));
		Integer fontTransparency = view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY);
		
		return mapFont(fontName, fontSize, fontColor, fontTransparency);
	}
}
