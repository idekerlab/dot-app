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

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.DOT;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.EQUAL_DASH;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.LONG_DASH;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.SOLID;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.DIAMOND;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.ELLIPSE;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.HEXAGON;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.OCTAGON;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.PARALLELOGRAM;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.RECTANGLE;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.ROUND_RECTANGLE;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.TRIANGLE;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles mapping of Cytoscape properties to .dot attributes in the form of a String.
 * Contains implementation for properties that are shared by nodes and edges and declarations
 * for unshared properties. Also contains variable definitions needed the subclasses
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public abstract class Mapper {

	// Maps Cytoscape properties  by their ID Strings to their .dot equivalents if relationship is simple equivalency
	protected ArrayList<String> simpleVisPropsToDot; 
	
	// VisualStyle applied to the view
	protected VisualStyle vizStyle;
	
	// view that this mapper object is mapping
	protected View<? extends CyIdentifiable> view;

	// If node width and height are locked
	private static boolean nodeSizesLockedIsSet = false;
	protected static boolean nodeSizesLocked;
	
	// Object that formats all of the numbers. Decimal separator is forced to '.' for GraphViz
	protected static DecimalFormat decimalFormatter = new DecimalFormat("#0.000000;-#0.000000");
	static {
		DecimalFormatSymbols formatSymbols = decimalFormatter.getDecimalFormatSymbols();
		formatSymbols.setDecimalSeparator('.');
		decimalFormatter.setDecimalFormatSymbols(formatSymbols);
	}
	
	/*
	 * Maps Cytoscape line types to the equivalent string used in .dot
	 * line types are fields in org.cytoscape.view.presentation.property.LineTypeVisualProperty
	 */ 
	protected static final HashMap<LineType, String> LINE_TYPE_MAP = new HashMap<LineType, String>();
	static {
		LINE_TYPE_MAP.put(LONG_DASH, "dashed");
		LINE_TYPE_MAP.put(EQUAL_DASH, "dashed");
		LINE_TYPE_MAP.put(SOLID, "solid");
		LINE_TYPE_MAP.put(DOT, "dotted");
	}
	
	/**
	 *  maps Cytoscape node shape types to the equivalent string used in .dot
	 */
	// node shapesare fields in org.cytoscape.view.presentation.property.NodeShapeVisualProperty
	protected static final HashMap<NodeShape, String> NODE_SHAPE_MAP = new HashMap<NodeShape, String>();
	static {
		NODE_SHAPE_MAP.put(TRIANGLE, "triangle");
		NODE_SHAPE_MAP.put(DIAMOND, "diamond");
		NODE_SHAPE_MAP.put(ELLIPSE, "ellipse");
		NODE_SHAPE_MAP.put(HEXAGON, "hexagon");
		NODE_SHAPE_MAP.put(OCTAGON, "octagon");
		NODE_SHAPE_MAP.put(PARALLELOGRAM, "parallelogram");
		NODE_SHAPE_MAP.put(ROUND_RECTANGLE, "rectangle");
		NODE_SHAPE_MAP.put(RECTANGLE, "rectangle");
	}
	
	/**
	 * Maps Cytoscape arrowhead types to the equivalent dot attribute
	 */
	// arrowheads come from org.cytoscape.view.presentation.property.ArrowShapeVisualProperty
	protected static final HashMap<ArrowShape, String> ARROW_SHAPE_MAP = new HashMap<ArrowShape, String>();
	static {
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.ARROW, "vee");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.CIRCLE, "dot");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.DELTA, "normal");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.DIAMOND, "diamond");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.HALF_BOTTOM, "ornormal");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.HALF_TOP, "olnormal");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.NONE, "none");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.T, "tee");
	}

	// Pixel per inch scaling factor
	protected static final double PPI = 72;

	// Logger that outputs to Cytoscape standard log file:  .../CytoscapeConfiguration/3/framework-cytoscape.log
	protected static final Logger LOGGER = LoggerFactory.getLogger(Mapper.class);

	/**
	 * Checks whether node size is locked,
	 * "Lock node and width height" checkbox
	 * 
	 * @param visualStyle VisualStyle being checked if node sizes are locked
	 * @return true if size is locked, false if not
	 */
	private static boolean areNodeSizesLocked(VisualStyle visualStyle) {
		LOGGER.info("Determining if NODE_HEIGHT/NODE_WIDTH are locked...");
		Set<VisualPropertyDependency<?>> vizDependencies = visualStyle.getAllVisualPropertyDependencies();
		boolean output = false;
		
		// go through all dependencies and find lock height and width one
		for(VisualPropertyDependency<?> dependency: vizDependencies) {
			LOGGER.info(dependency.getIdString());
			if((dependency.getIdString()).equals("nodeSizeLocked")) {
				output = dependency.isDependencyEnabled();
			}
		}

		return output;
	}
	/**
	 * Used to change an id string of a graph element to comply with dot ID requirements
	 * Dot names must be a string of alphanumeric characters and underscores,
	 * not beginning with a digit
	 * 
	 * @param id is String we are modifying
	 * @return is .dot-compliant ID String where all leading numbers are removed
	 * and put at the end of the string and all dis-allowed characters are replaced
	 * with underscores
	 */
	public static String modifyElementID(String id) {
		LOGGER.trace("Preparing to transform ID");
		String alphaNumRegEx = "[a-zA-Z\200-\377_][a-zA-Z\200-\377_0-9]*";
		String numericRegEx = "[-]?([.][0-9]+|[0-9]+([.][0-9]*)?)";
		String quotedRegEx = "\"[^\"]*(\\\")*[^\"]*\"";
		String htmlRegEx = "<.*>";
		if (id.matches(alphaNumRegEx)) {
			LOGGER.trace("Passed-in ID is an Alphanumeric ID");
			return id;
		}
		if (id.matches(numericRegEx)) {
			LOGGER.trace("Passed-in ID is a Numeric ID");
			return id;
		}
		if (id.matches(quotedRegEx)) {
			LOGGER.trace("Passed-in ID is a Quoted ID");
			return id;
		}
		if (id.matches(htmlRegEx)) {
			LOGGER.trace("Passed-in ID is an HTML ID");
			return id;
		}
		LOGGER.trace("None of the above. Transforming to Quoted ID");
		StringBuilder output = new StringBuilder(id.length() + 2);
		output.append('\"');
		// replace any quotations from name string with escaped quotes
		id = id.replace("\\", "\\\\");
		id = id.replace("\"", "\\\"");
		output.append(id);
		output.append('\"');
		return output.toString();
	}
	
	
	/**
	 * Constructor for Mapper objects
	 * 
	 * @param view View being mapped to dot by this mapper
	 * @param vizStyle Visual Style being applied to the view
	 */
	public Mapper(View<? extends CyIdentifiable> view, VisualStyle vizStyle) {
		this.view = view;
		this.vizStyle = vizStyle;
		if (!nodeSizesLockedIsSet) {
			nodeSizesLocked = areNodeSizesLocked(vizStyle);
			nodeSizesLockedIsSet = true;
		}
	}
	
	/**
	 * Checks whether a value is equal to the default value set for a
	 * VisualProperty by the Visual Style applied to the network
	 * 
	 * @param val The value being checked
	 * @param vizProp VisualProperty against which val is being checked
	 * @return boolean. True when value is equal, false if not
	 */
	protected <T> boolean isEqualToDefault(T val, VisualProperty<T> vizProp) {
		return val.equals(vizStyle.getDefaultValue(vizProp));
	}
	
	/**
	 * Checks whether the value of a VisualProperty applied to the view is equal
	 * to the default value set for that VP by the Visual Style applied to the network
	 * 
	 * @param vizProp VisualProperty being compared
	 * @return boolean. True when value is equal, false if not
	 */
	protected boolean isEqualToDefault(VisualProperty<?> vizProp) {
		return view.getVisualProperty(vizProp).equals(vizStyle.getDefaultValue(vizProp));
	}
	
	/**
	 * Given a color, returns the color in String format that .dot uses for color.
	 * Format is "#rrggbbaa" -- red, green, blue, alpha in hexadecimal
	 * 
	 * @param color color being converted
	 * @param alpha alpha level of that color-- cytoscape does not use alpha in Paint class
	 * @return String representation of color in .dot format of rgba
	 */
	protected String mapColorToDot(Color color, Integer alpha) {
		LOGGER.trace("Creating .dot color attribute string");
		Integer red = color.getRed();
		Integer green = color.getGreen();
		Integer blue = color.getBlue();
		String result = String.format("#%02X%02X%02X%02X", red, green, blue, alpha);
		LOGGER.debug("Created .dot color attribute string. Result: " + result);
		return result;
	}
	
	/**
	 * Returns the .dot equivalent in String form for style attribute. Only handles linestyle
	 * Does not include "style=" bit
	 */
	abstract protected String mapDotStyle();
	
	/**
	 * Given a font, returns the .dot equivalent in String form including the
	 * font name, size and color
	 * 
	 * @param font font to be converted
	 * @param size size of font to be converted
	 * @param color color of font
	 * @param transparency transparency of font from 0-255
	 * @return String that is .dot representation of the provided font
	 */
	protected String mapFont(Font font, Integer size, Color color, Integer transparency) {
		
		LOGGER.trace("Label font, size, color, and transparency translation");
		 
		StringBuilder returnValue = null;
		
		if (view.getModel() instanceof CyNode) {
			LOGGER.trace("Mapping font attributes for a node view...");
			LOGGER.trace("Determining need for fontname attr");
			if (!isEqualToDefault(font, NODE_LABEL_FONT_FACE)) {
				Font styleFont = vizStyle.getDefaultValue(NODE_LABEL_FONT_FACE);
				if (!font.getFontName().equals(styleFont.getFontName()) || 
						!font.getFamily().equals(styleFont.getFamily())) {
					String fontName = String.format("fontname = \"%s\"", font.getFontName());
					if (returnValue == null) {
						returnValue = new StringBuilder(fontName);
					}
				}
			}
			LOGGER.trace("Determining need for fontsize attr");
			if (!isEqualToDefault(size, NODE_LABEL_FONT_SIZE)) {
				String fontSize = String.format("fontsize = \"%d\"", size);
				if (returnValue == null) {
					returnValue = new StringBuilder(fontSize);
				} 
				else {
					returnValue.append(","+fontSize);
				}
			}
			LOGGER.trace("Determining need for fontcolor attr");
			if (!isEqualToDefault(color, NODE_LABEL_COLOR) ||
					!isEqualToDefault(transparency, NODE_LABEL_TRANSPARENCY)) {
				String fontColor = String.format("fontcolor = \"%s\"", mapColorToDot(color, transparency));
				if (returnValue == null) {
					returnValue = new StringBuilder(fontColor);
				} 
				else {
					returnValue.append(","+fontColor);
				}
			}
		} 
		// had an ugly conflict in this area. note if there are font problems it could be from here
		else if (view.getModel() instanceof CyEdge) {
			LOGGER.trace("Mapping font attributes for an edge view...");
			LOGGER.trace("Determining need for fontname attr");
			if (!isEqualToDefault(font, EDGE_LABEL_FONT_FACE)) {
				Font styleFont = vizStyle.getDefaultValue(EDGE_LABEL_FONT_FACE);
				if (!font.getFontName().equals(styleFont.getFontName()) || 
						!font.getFamily().equals(styleFont.getFamily())) {
					String fontName = String.format("fontname = \"%s\"", font.getFontName());
					if (returnValue == null) {
						returnValue = new StringBuilder(fontName);
					}
				}
			}
			LOGGER.trace("Determining need for fontsize attr");
			if (!isEqualToDefault(size, EDGE_LABEL_FONT_SIZE)) {
				String fontSize = String.format("fontsize = \"%d\"", size);
				if (returnValue == null) {
					returnValue = new StringBuilder(fontSize);
				} 
				else {
					returnValue.append(","+fontSize);
				}
			}
			LOGGER.trace("Determining need for fontcolor attr");
			if (!isEqualToDefault(color, EDGE_LABEL_COLOR) ||
					!isEqualToDefault(transparency, EDGE_LABEL_TRANSPARENCY)) {
				String fontColor = String.format("fontcolor = \"%s\"", mapColorToDot(color, transparency));
				if (returnValue == null) {
					returnValue = new StringBuilder(fontColor);
				} 
				else {
					returnValue.append(","+fontColor);
				}
			}
		}

		if (returnValue == null) {
			LOGGER.trace("Font attributes are defaults");
			return null;
		}
		LOGGER.debug("Dot attributes associate with font is: " + returnValue.toString());
		
		return returnValue.toString();		
		
	}

	/**
	 * Returns the String that denotes a position in .dot format
	 * Note: Positive in graphviz is up and right, positive in cytoscape
	 * is down and right. Therefore, we negate the y-values
	 * 
	 * @param x x coordinate of position
	 * @param y y coordinate of position
	 * @return String in form %x,%y
	 */
	protected String mapPosition(Double x, Double y) {
		/*x /= PPI;
		y /= PPI;*/
		return String.format("%s,%s", decimalFormatter.format(x), decimalFormatter.format(-1*y));
	}	

	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	public abstract String getElementString();
}
