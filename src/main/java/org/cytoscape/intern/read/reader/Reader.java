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

package org.cytoscape.intern.read.reader;

import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.DOT;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.EQUAL_DASH;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.SOLID;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualStyle;

import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class that contains definitions and some implementation for converting a
 * dot graph to a CyNetwork. Data is passed in as JPGD objects
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public abstract class Reader {
	
	// Logger that outputs to Cytoscape standard log file:  .../CytoscapeConfiguration/3/framework-cytoscape.log
	protected static final Logger LOGGER = LoggerFactory.getLogger(Reader.class);

	// Color enum representing the different Graphviz color attributes
	protected static enum ColorAttribute {
		COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	}

	//Regex patterns for DOT color strings
	private static final String RGB_REGEX = "^#[0-9A-Fa-f]{6}$";
	private static final String RGBA_REGEX = "^#(?<RED>[0-9A-Fa-f]{2})"
						   + "(?<GREEN>[0-9A-Fa-f]{2})"
						   + "(?<BLUE>[0-9A-Fa-f]{2})"
						   + "(?<ALPHA>[0-9A-Fa-f]{2})$";
	private static final String HSB_REGEX = "^(?<HUE>1(?:\\.0+)?|0*(?:\\.[0-9]+))(?:,|\\s)+"
						 + "(?<SAT>1(?:\\.0+)?|0*(?:\\.[0-9]+))(?:,|\\s)+"
						 + "(?<VAL>1(?:\\.0+)?|0*(?:\\.[0-9]+))$";


	// Maps lineStyle attribute values to Cytoscape values
	protected static final Map<String, LineType> LINE_TYPE_MAP = new HashMap<String, LineType>();
	static {
		LINE_TYPE_MAP.put("dotted", DOT);
		LINE_TYPE_MAP.put("dashed", EQUAL_DASH);
		LINE_TYPE_MAP.put("solid", SOLID);
	}
	
	// Maps string color names to Java Color objects
	protected static StringColor stringColors;

	// view of network being created/modified
	protected CyNetworkView networkView;
	
	// visualStyle being applied to network, used to set default values
	protected VisualStyle vizStyle;
	
	//True if "fillcolor" attribute has already been consumed for VisualStyle
	protected boolean hasUsedDefFill = false;
	/*
	 * Map of explicitly defined default attributes
	 * key is attribute name, value is value
	 */
	protected Map<String, String> defAttrs;
	
	// VisualLexicon containing definitions of all VisualProperties
	// Used for compatibility with "Ding" specific VisualProperties
	protected VisualLexicon vizLexicon;	

	/*
	 * Contains elements of Cytoscape graph and their corresponding JPGD elements
	 * is null for NetworkReader. Is initialized on Node, Edge Reader
	 */
	protected Map<? extends Object, ? extends CyIdentifiable> elementMap;
	

	/**
	 * Constructs an object of type Reader.
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader of this type
	 * eg. for NodeReader will be a list of default
	 * @param rendEngMgr RenderingEngineManager that contains the default
	 * VisualLexicon needed for gradient support
	 */
	public Reader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, RenderingEngineManager rendEngMgr) {

		this.networkView = networkView;
		this.vizStyle = vizStyle;
		this.defAttrs = defaultAttrs;
		this.vizLexicon = rendEngMgr.getDefaultVisualLexicon();
	}
	
	/**
	 * Sets all the default Visual Properties values of the Cytoscape
	 * VisualStyle. Subclasses handle different visual properties
	 * (eg. NetworkReader sets all network props, NodeReader sets all node
	 * properties, and EdgeReader sets all edge properties)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setDefaults() {
		LOGGER.info("Setting the Default values for Visual Style...");

		String colorScheme = defAttrs.containsKey("colorscheme") ? defAttrs.get("colorscheme") : null;
		for (Entry<String, String> attrEntry : defAttrs.entrySet()) {
			String attrKey = attrEntry.getKey();
			String attrVal = attrEntry.getValue();
			LOGGER.debug(
				String.format("Converting DOT attribute: %s", attrKey)
			);

			Pair<VisualProperty, Object> p = convertAttribute(attrKey, attrVal);
			// if attribute cannot be converted, move on to next one
			if (p == null) {
				continue;
			}

			// set in vizStyle
			VisualProperty vizProp = p.getLeft();
			Object val = p.getRight();
			if (vizProp == null || val == null) {
				continue;
			}
			LOGGER.trace("Updating Visual Style...");
			LOGGER.debug("Setting Visual Property {} with value {}", vizProp, val);
			vizStyle.setDefaultValue(vizProp, val);
		}
		
		setColorDefaults(vizStyle, colorScheme);
		// Set style attribute here so we handle node shape dependency
		String styleAttr = defAttrs.containsKey("style") ? defAttrs.get("style") : "";
		setStyle(styleAttr, vizStyle);
	}


	/**
	 * Converts the specified GraphViz attribute and value to its Cytoscape 
	 * equivalent VisualProperty and VisualPropertyValue. If an equivalent value
	 * is not found, then a default Cytoscape VisualPropertyValue is used.
	 * This method only handles GraphViz attributes that do not correspond to
	 * more than one Cytoscape VisualProperty.
	 * 
	 * @param name the name of the attribute
	 * @param val the value of the attribute
	 * 
	 * @return Pair object of which the left value is the VisualProperty and the right value
	 * is the VisualPropertyValue.
	 */
	@SuppressWarnings("rawtypes")
	abstract protected Pair<VisualProperty, Object> convertAttribute(String name, String val); 

	/**
	 * Converts a GraphViz color string to a Java Color object
	 * GraphViz color formats are:
	 * #RRGGBB (in hex)
	 * #RRGGBBAA (in hex)
	 * H S V (0 <= Hue, saturation, value <= 1.0)
	 * String that is name of color from a GraphViz color scheme
	 *  
	 * @param color Color from dot file-- takes all color formats
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	protected Color convertColor(String color, String colorScheme) {

		LOGGER.info("Converting DOT color string to Java Color...");
		LOGGER.debug("Color string: " + color);

		// Remove trailing/leading whitespace
		color = color.trim();

		// Test color string against RGB regex
		LOGGER.trace("Comparing DOT color string to #RRGGBB format");
		Matcher matcher = Pattern.compile(RGB_REGEX).matcher(color);
		if (matcher.matches()) {
			return Color.decode(color);
		}

		// Test color string against RGBA regex
		LOGGER.trace("Comparing DOT color string to #RRGGBBAA format");
		matcher.usePattern(Pattern.compile(RGBA_REGEX));
		if (matcher.matches()) {
			Integer red = Integer.valueOf(matcher.group("RED"), 16);
			Integer green = Integer.valueOf(matcher.group("GREEN"), 16);
			Integer blue = Integer.valueOf(matcher.group("BLUE"), 16);
			Integer alpha = Integer.valueOf(matcher.group("ALPHA"), 16);
			return new Color(red, green, blue, alpha);
		}

		// Test color string against HSB regex
		LOGGER.trace("Comparing DOT color string to H S V format");
		matcher.usePattern(Pattern.compile(HSB_REGEX));
		if (matcher.matches()) {
			Float hue = Float.valueOf(matcher.group("HUE"));
			Float saturation = Float.valueOf(matcher.group("SAT"));
			Float value = Float.valueOf(matcher.group("VAL"));
			return Color.getHSBColor(hue, saturation, value);
		}
		
		// String didn't match the regexes, so test if it is a color name
		// Read in color name files and find it in there
		LOGGER.trace("Checking if DOT color string is a valid color name");
		if (stringColors == null) {
			stringColors = new StringColor("svg_colors.txt", "x11_colors.txt");
		}
		Color output = stringColors.getColor(colorScheme, color);
		if(output != null) {
			return output;
		}
		// Color was not found
		LOGGER.info("DOT color string not supported.");
		return null;
	}
	
	/**
	 * Converts a GraphViz colorlist string to a list of Pairs containing
	 * Colors and Floats
	 * GraphViz colorlist format is WC(:WC)*,
	 * of which WC is C[;F],
	 * of which C is a Graphviz color, and F is a float 0.0 <= x <= 1.0
	 *  
	 * @param color Graphviz colorlist
	 * @param colorScheme Color Scheme used to translate color names
	 */
	protected List<Pair<Color, Float>> convertColorList(String colorList, String colorScheme) {
		LOGGER.info("Converting DOT color list to Java aray...");
		//Split color list into weighted colors
		if (!colorList.contains(":")) {
			return null;
		}
		String[] weightedColors = colorList.split(":");
		int numColors = 0;
		ArrayList<Pair<Color, Float>> colorWeightPairs = new ArrayList<Pair<Color,Float>>(weightedColors.length);
		for (String weightedColor : weightedColors) {
			if (numColors == 2) {
				break;
			}
			if (weightedColor.contains(";")) {
				String[] color_weight = weightedColor.split(";", 2);
				Color color = convertColor(color_weight[0], colorScheme);
				Float weight = null;
				try {
					weight = Float.parseFloat(color_weight[1]);
				}
				catch (NumberFormatException exception) {
					LOGGER.error("Error: Color list contains invalid weight");
				}
				LOGGER.debug(String.format("Retrieved weighted color from color list. Result: %s;%f", color.toString(), weight));
				colorWeightPairs.add(Pair.of(color, weight));
			}
			else {
				Color color = convertColor(weightedColor, colorScheme);
				LOGGER.debug(String.format("Retrieved color with no weight from color list. Result: %s", color.toString()));
				colorWeightPairs.add(Pair.of(color, (Float)null));
			}
			numColors++;
		}
		return colorWeightPairs;
	}

	/**
	 * Returns the Map of bypass attributes for a given JPGD object.
	 * We define bypass attributes as any attributes declared at the individual
	 * GraphViz element declaration.
	 * 
	 * @param element JPGD element for which we are getting list of attributes.
	 * Should be an instance of Node or Edge.
	 * @return Map of which keys are attribute names and values are attribute
	 * values
	 * @throws IllegalArgumentException thrown if passed-in object is not an
	 * instance of Node or Edge
	 */
	protected Map<String, String> getAttrMap(Object element) {
	
		if (element instanceof Node) {
			return ((Node) element).getAttributes();
		}
		if (element instanceof Edge) {
			return ((Edge) element).getAttributes();
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * Sets all the bypass Visual Properties values for View objects in
	 * Cytoscape. Implemented in subclasses to handle different subclasses of
	 * View objects
	 */
	abstract protected void setBypasses();
	
	/**
	 * Converts a GraphViz color attribute into a VisualProperty bypass value
	 * for a Cytoscape View object
	 * 
	 * @param attrVal GraphViz color string
	 * @param elementView View of Cytoscape element to which a color
	 * VisualProperty is being set
	 * @param attr enum for type of color: COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	abstract protected void setColor(String attrVal, View<? extends CyIdentifiable> elementView, ColorAttribute attr, String colorScheme);
 
	/**
	 * Converts a GraphViz color attribute into a default VisualProperty value
	 * for a Cytoscape VisualStyle
	 * 
	 * @param attrVal GraphViz color string
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	abstract protected void setColor(String attrVal, VisualStyle vizStyle, ColorAttribute attr, String colorScheme);

	/**
	 * Sets all the default values of Color VisualProperties for the VisualStyle.
	 * Subclasses implement this method to handle the different CyIdentifiables
	 */
	abstract protected void setColorDefaults(VisualStyle vizStyle, String colorScheme);

	/**
	 * Converts the GraphViz "style" attribute into VisualProperty bypass values
	 * for a Cytoscape View object
	 * 
	 * @param attrVal String that is the value of "style" (eg. "dashed, round")
	 * @param elementView view to which "style" is being applied
	 */
	abstract protected void setStyle(String attrVal, View<? extends CyIdentifiable> elementView);

	/**
	 * Converts the GraphViz "style" attribute into default VisualProperty
	 * values for a Cytoscape VisualStyle
	 * 
	 * @param attrVal String that is the value of "style" 
	 * eg. "dashed, rounded"
	 * @param vizStyle VisualStyle that "style" is being applied to
	 */
	abstract protected void setStyle(String attrVal, VisualStyle vizStyle);

	/**
	 * Sets default VisualProperties and bypasses for each element in list.
	 * Children classes may override this method, with a super call, to handle
	 * exception properties such as location and edge weights
	 */
	public void setProperties() {
		LOGGER.info("Setting the properties for Visual Style...");
		setDefaults();
		setBypasses();
	}
}

















