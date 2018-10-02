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
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.ROUND_RECTANGLE;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * Handles mapping of CyNode properties to .dot equivalent Strings
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NodePropertyMapper extends Mapper {
	
	// location of node label
	private String labelLoc;
	
	/**
	 * Initializes and populates instance variables with mappings
	 * 
	 * @param view View of Node we are converting to .dot
	 */
	public NodePropertyMapper(View<CyNode> view, VisualStyle vizStyle, String labelLoc) {
		super(view, vizStyle);
		// initialize data structure
		simpleVisPropsToDot = new ArrayList<String>();
		this.labelLoc = labelLoc;
		
		populateMaps();
	}
	
	/**
	 * Helper method that returns String that defines color attribute including "fillcolor=" part
	 * 
	 * @return String in form "color = <color>,fillcolor = <color>"
	 */
	private String mapColors() {
		StringBuilder elementString = null;
		
		LOGGER.debug("Preparing to get color properties for a node");
		// Get the color string (border color). Append to attribute string
		if (!isEqualToDefault(NODE_BORDER_PAINT) || !isEqualToDefault(NODE_BORDER_TRANSPARENCY)) {
			Color borderColor = (Color) view.getVisualProperty(NODE_BORDER_PAINT);
			Integer borderTransparency = ((Number)view.getVisualProperty(NODE_BORDER_TRANSPARENCY)).intValue();
			String dotBorderColor = String.format("color = \"%s\"", mapColorToDot(borderColor, borderTransparency));
			elementString = new StringBuilder(dotBorderColor);
		}
		
		// Write node fill color
		if (!isEqualToDefault(NODE_FILL_COLOR) || !isEqualToDefault(NODE_TRANSPARENCY)) {
			Color fillColor = (Color) view.getVisualProperty(NODE_FILL_COLOR);
			Integer transparency = ((Number)view.getVisualProperty(NODE_TRANSPARENCY)).intValue();
			String dotFillColor = String.format("fillcolor = \"%s\"", mapColorToDot(fillColor, transparency));
			if (elementString != null) {
				elementString.append("," + dotFillColor);
			}
			else {
				elementString = new StringBuilder(dotFillColor);
			}
		}
		if (elementString == null) {
			return null;
		}
		return elementString.toString();
		
	}
	
	/**
	 * Helper method that returns String that contains font face, size, color and transparency
	 * 
	 * @return String that defines fontname, fontcolor and fontsize attributes
	 */
	private String mapFontHelper() {
		LOGGER.debug("Getting the label related attributes for a node");
		Font fontName = view.getVisualProperty(NODE_LABEL_FONT_FACE);
		Integer fontSize = ((Number)view.getVisualProperty(NODE_LABEL_FONT_SIZE)).intValue();
		Color fontColor = (Color)(view.getVisualProperty(NODE_LABEL_COLOR));
		Integer fontTransparency = ((Number)view.getVisualProperty(NODE_LABEL_TRANSPARENCY)).intValue();
		
		return mapFont(fontName, fontSize, fontColor, fontTransparency);
	}
	
	/**
	 * Helper method that returns String that represents nodeShape 
	 * 
	 * @return String in form in form "shape = <shape>"
	 */
	private String mapShape() {
		LOGGER.debug("Preparing to get shape property");
		
		// Get the .dot string for the node shape. Append to attribute string
		if (isEqualToDefault(NODE_SHAPE)) {
			return null;
		}
		NodeShape shape = view.getVisualProperty(NODE_SHAPE);
		String shapeStr = NODE_SHAPE_MAP.get(shape);
		
		// default if there is no match
		if (shapeStr == null) {
			shapeStr = "rectangle"; 
			LOGGER.warn("Cytoscape property doesn't map to a .dot attribute. Setting to default");
		}
		
		String dotShape = String.format("shape = \"%s\"", shapeStr);
		LOGGER.debug("Appended shape attribute to .dot string. Result: " + dotShape);
		
		return dotShape;
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		LOGGER.trace("Populating HashMaps with values");

		// Put Simple Props Key/Values
		
		// determine if using exlabel attribute or not
		String nodeLabel = view.getVisualProperty(NODE_LABEL);
		// Replace quotes with escaped quotes if any
		nodeLabel = nodeLabel.replace("\\", "\\\\");
		nodeLabel = nodeLabel.replace("\"", "\\\"");
		// if internal label
		if (!isEqualToDefault(NODE_LABEL)) {
			if(!labelLoc.equals("ex")) {
				simpleVisPropsToDot.add(String.format("label = \"%s\"", nodeLabel));
			}
			// if external label
			else {
				simpleVisPropsToDot.add(String.format("xlabel = \"%s\"", nodeLabel));
			}
		}
		
		if (!isEqualToDefault(NODE_BORDER_WIDTH)) {
			Double borderWidth = view.getVisualProperty(NODE_BORDER_WIDTH);
			simpleVisPropsToDot.add(String.format("penwidth = \"%s\"", decimalFormatter.format(borderWidth)));
		}

		// Get node height and width
		if(nodeSizesLocked) {
			/* 
			 * view.getVisualProperty(NODE_SIZE) does not return the actual
			 * dimension of the node view when a mapping is applied to
			 * NODE_SIZE, so getting dimension from NODE_HEIGHT instead
			 */
			
			Double size = view.getVisualProperty(NODE_HEIGHT);
			if(!isEqualToDefault(size, NODE_SIZE)){
				simpleVisPropsToDot.add(String.format("height = \"%s\"", decimalFormatter.format(size/PPI)));
				simpleVisPropsToDot.add(String.format("width = \"%s\"", decimalFormatter.format(size/PPI)));
			}
		}
		else {
			if(!isEqualToDefault(NODE_HEIGHT)) {
				Double height = view.getVisualProperty(NODE_HEIGHT);
				simpleVisPropsToDot.add(String.format("height = \"%s\"", decimalFormatter.format(height/PPI)));
			}
			if(!isEqualToDefault(NODE_WIDTH)) {
				Double width = view.getVisualProperty(NODE_WIDTH);
				simpleVisPropsToDot.add(String.format("width = \"%s\"", decimalFormatter.format(width/PPI)));
			}
		}

		// Get node tooltip
		if (!isEqualToDefault(NODE_TOOLTIP)) {
			String tooltip = view.getVisualProperty(NODE_TOOLTIP);
			simpleVisPropsToDot.add(String.format("tooltip = \"%s\"", tooltip));
		}
		
        // Get node location and append in proper format
        Double xLoc = view.getVisualProperty(NODE_X_LOCATION);
        Double yLoc = view.getVisualProperty(NODE_Y_LOCATION);
        String dotPosition = String.format("pos = \"%s\"", mapPosition(xLoc, yLoc));
        simpleVisPropsToDot.add(dotPosition);
		
		// Put Node Shape Key/Values
		LOGGER.trace("HashMaps populated");
	}
	
	@Override
	protected String mapDotStyle() {
		LOGGER.trace("Building style string for node view...");
		StringBuilder dotStyle = null;
		if (!isEqualToDefault(NODE_BORDER_LINE_TYPE) || !isEqualToDefault(NODE_SHAPE)
			|| !isEqualToDefault(NODE_VISIBLE)) {
			LOGGER.info("Not default style attr, building node's own...");
			LineType lineType = view.getVisualProperty(NODE_BORDER_LINE_TYPE);
			NodeShape nodeShape = view.getVisualProperty(NODE_SHAPE);
			String lineStr = "";
			dotStyle = new StringBuilder();

			// get .dot equivalent of line style, see if we need rounded
			lineStr = LINE_TYPE_MAP.get(lineType);
			if (lineStr == null) {
				lineStr = "solid";
				LOGGER.warn("Cytoscape property doesn't map to a .dot attribute. Setting to default");
			}
			boolean rounded = nodeShape.equals(ROUND_RECTANGLE);
			boolean isVisible = view.getVisualProperty(NODE_VISIBLE);
			String roundedString = (rounded) ? "rounded," : "";
			String invisString = (!isVisible) ? "invis," : "";
           	String style = String.format("style = \"%s,%s%sfilled\"", lineStr, roundedString, invisString);
           	dotStyle.append(style);
		}
		if (dotStyle == null) {
			return null;
		}
		return dotStyle.toString();
	}

	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		LOGGER.trace("Preparing to get .dot declaration for a node.");

		// Build attribute string
		StringBuilder elementString = new StringBuilder("[");

		// Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot) {
		        elementString.append(dotAttribute);
		        elementString.append(",");
		}
		
		// Write fillcolor and color attribute
		String colorsString = mapColors();
		if (colorsString != null) {
			elementString.append(colorsString + ",");
		}

		// Write nodeShape
		String shapeString = mapShape();
		if (shapeString != null) {
			elementString.append(mapShape() + ",");
		}
		

		// Get the .dot string for the node style. Append to attribute string
		String styleString = mapDotStyle();
		if (styleString != null) {
			elementString.append(styleString + ",");
		}
		
		// Append font name+size+color attributes
		String fontString = mapFontHelper();
		if (fontString != null) {
			elementString.append(fontString);
		}

		// Finish Attribute List
		if (elementString.charAt(elementString.length() - 1) == ',') {
			elementString.deleteCharAt(elementString.length() - 1);
		}
		elementString.append("]");
		String result = elementString.toString();
		if (result.matches("^\\[\\]$")) {
			result = "";
		}
		LOGGER.debug("Created .dot string. Result: " + result);
		return result;
	}
}
