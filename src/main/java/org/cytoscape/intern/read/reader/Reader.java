package org.cytoscape.intern.read.reader;

import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.DOT;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.EQUAL_DASH;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.SOLID;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.intern.FileHandlerManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualStyle;

import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Node;

/**
 * Abstract class that contains definitions and some implementation for converting a
 * dot graph to a CyNetwork. Data is passed in as JPGD objects
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public abstract class Reader {
	
	//debug logger declaration 
	protected static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.Reader");
	protected static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();
	static {
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_Reader.txt");
			handler.setLevel(Level.ALL);
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);
	}

	// view of network being created/modified
	protected CyNetworkView networkView;

	// visualStyle being applied to network, used to set default values
	protected VisualStyle vizStyle;

	//True if "fillcolor" attribute has already been consumed for VisualStyle
	protected boolean usedDefaultFillColor = false;
	/*
	 * Map of explicitly defined default attributes
	 * key is attribute name, value is value
	 */
	protected Map<String, String> defaultAttrs;
	
	// Maps lineStyle attribute values to Cytoscape values
	protected static final Map<String, LineType> LINE_TYPE_MAP = new HashMap<String, LineType>();
	static {
		LINE_TYPE_MAP.put("dotted", DOT);
		LINE_TYPE_MAP.put("dashed", EQUAL_DASH);
		LINE_TYPE_MAP.put("solid", SOLID);
	}
	
	protected static enum ColorAttribute {
		COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	}	

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
	 * @param defaultAttrs Map that contains default attributes
	 * 
	 */
	public Reader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs) {

		this.networkView = networkView;
		this.vizStyle = vizStyle;
		this.defaultAttrs = defaultAttrs;
	}
	
	/**
	 * Sets all the default Visual Properties values of the Cytoscape
	 * VisualStyle. Subclasses handle different visual properties
	 * (eg. NetworkReader sets all network props, NodeReader sets all node
	 * properties, and EdgeReader sets all edge properties)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setDefaults() {
		LOGGER.info("Setting the Default values for Visual Style...");

		for (Entry<String, String> attrEntry : defaultAttrs.entrySet()) {
			String attrKey = attrEntry.getKey();
			String attrVal = attrEntry.getValue();
			LOGGER.info(
				String.format("Converting DOT attribute: %s", attrKey)
			);

			if (attrKey.equals("style")) {
				setStyle(attrVal, vizStyle);
				// this attribute has been handled, move on to next one
				continue;
			}
			if (attrKey.equals("color") || attrKey.equals("fillcolor")
					|| attrKey.equals("fontcolor") || attrKey.equals("bgcolor")) {
				switch (attrKey) {
					case "color": {
						setColor(attrVal, vizStyle, ColorAttribute.COLOR);
						break;
					}
					case "fillcolor": {
						setColor(attrVal, vizStyle, ColorAttribute.FILLCOLOR);
						usedDefaultFillColor = true;
						break;
					}
					case "fontcolor": {
						setColor(attrVal, vizStyle, ColorAttribute.FONTCOLOR);
						break;
					}
					case "bgcolor": {
						setColor(attrVal, vizStyle, ColorAttribute.BGCOLOR);
						break;
					}
				}
				// this attribute has been handled, move on to next one
				continue;
			}

			Pair<VisualProperty, Object> p = convertAttribute(attrEntry.getKey(), attrEntry.getValue());
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
			LOGGER.info("Updating Visual Style...");
			LOGGER.info(String.format("Setting Visual Property %S...", vizProp));
			vizStyle.setDefaultValue(vizProp, val);
		}
	}

	/**
	 * Sets all the bypass Visual Properties values for View objects in
	 * Cytoscape. Implemented in subclasses to handle different subclasses of
	 * View objects
	 */
	abstract protected void setBypasses();
	
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
	 * Converts a GraphViz color string to a Java Color object
	 * GraphViz color formats are:
	 * #RRGGBB (in hex)
	 * #RRGGBBAA (in hex)
	 * H S V (0 <= Hue, saturation, value <= 1.0)
	 * String that is name of color from a GraphViz color scheme
	 *  
	 * @param color GraphViz color string to be converted
	 */
	protected Color convertColor(String color) {

		LOGGER.info(String.format("GraphViz color string: %s", color));
		LOGGER.info("Converting GraphViz color string to Java Color...");

		//Remove trailing/leading whitespace
		color = color.trim();

		// if color is a list-- will support later. For now, take first color 
		if(color.contains(";") || color.contains(":")) {
			
			int colonIndex = color.indexOf(':');
			int semicolonIndex = color.indexOf(';');
			int firstIndex = (colonIndex > semicolonIndex) ? semicolonIndex : colonIndex;
			
			color = color.substring(0, firstIndex);
		}

		//Regex patterns for DOT color strings
		String rgbRegex = "^#[0-9A-Fa-f]{6}$";

		String rgbaRegex = "^#(?<RED>[0-9A-Fa-f]{2})"
						   + "(?<GREEN>[0-9A-Fa-f]{2})"
						   + "(?<BLUE>[0-9A-Fa-f]{2})"
						   + "(?<ALPHA>[0-9A-Fa-f]{2})$";

		String hsbRegex = "^(?<HUE>1(?:\\.0+)?|0*(?:\\.[0-9]+))(?:,|\\s)+"
						 + "(?<SAT>1(?:\\.0+)?|0*(?:\\.[0-9]+))(?:,|\\s)+"
						 + "(?<VAL>1(?:\\.0+)?|0*(?:\\.[0-9]+))$";

		// Test color string against RGB regex
		LOGGER.info("Comparing DOT color string to #FFFFFF format");
		Matcher matcher = Pattern.compile(rgbRegex).matcher(color);
		if (matcher.matches()) {
			return Color.decode(color);
		}

		// Test color string against RGBA regex
		LOGGER.info("Comparing DOT color string to #FFFFFFFF format");
		matcher.usePattern(Pattern.compile(rgbaRegex));
		if (matcher.matches()) {
			Integer red = Integer.valueOf(matcher.group("RED"), 16);
			Integer green = Integer.valueOf(matcher.group("GREEN"), 16);
			Integer blue = Integer.valueOf(matcher.group("BLUE"), 16);
			Integer alpha = Integer.valueOf(matcher.group("ALPHA"), 16);
			return new Color(red, green, blue, alpha);
		}

		// Test color string against HSB regex
		LOGGER.info("Comparing DOT color string to H S V format");
		matcher.usePattern(Pattern.compile(hsbRegex));
		if (matcher.matches()) {
			Float hue = Float.valueOf(matcher.group("HUE"));
			Float saturation = Float.valueOf(matcher.group("SAT"));
			Float value = Float.valueOf(matcher.group("VAL"));
			return Color.getHSBColor(hue, saturation, value);
		}
		
		// Color did not match any of the regexes and is not color valid string.
		// return a default color
		LOGGER.info("DOT color string not supported. Return default color.");
		return Color.BLUE;
	}
	

	/**
	 * Converts the specified GraphViz attribute and value to its Cytoscape 
	 * equivalent VisualProperty and VisualPropertyValue. If an equivalent value
	 * is not found, then a default Cytoscape VisualPropertyValue is used
	 * 
	 * @param name the name of the attribute
	 * @param val the value of the attribute
	 * 
	 * @return Pair object of which the left value is the VisualProperty and the right value
	 * is the VisualPropertyValue.
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Pair<VisualProperty, Object> convertAttribute(String name, String val);

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
	 * Converts the GraphViz "style" attribute into VisualProperty bypass values
	 * for a Cytoscape View object
	 * 
	 * @param attrVal String that is the value of "style" (eg. "dashed, round")
	 * @param elementView View of Cytoscape element to which "style" is being
	 * applied (eg. View<CyNode>)
	 */
	abstract protected void setStyle(String attrVal, View<? extends CyIdentifiable> elementView);

	/**
	 * Converts a GraphViz color attribute into a default VisualProperty value
	 * for a Cytoscape VisualStyle
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	 */
	abstract protected void setColor(String attrVal, VisualStyle vizStyle, ColorAttribute attr);

	/**
	 * Converts a GraphViz color attribute into a VisualProperty bypass value
	 * for a Cytoscape View object
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param elementView View of Cytoscape element to which a color 
	 * VisualProperty being set
	 * @param attr enum for type of color: COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	 */
	abstract protected void setColor(String attrVal, View<? extends CyIdentifiable> elementView, ColorAttribute attr);
}

















