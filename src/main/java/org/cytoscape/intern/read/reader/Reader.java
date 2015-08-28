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
import org.cytoscape.model.CyNetwork;
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
	 * Constructs an object of type Reader. Sets up Logger.
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader of this type
	 * eg. for NodeReader will be a list of default
	 */
	public Reader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs) {

		// Make logger write to file

		this.networkView = networkView;
		this.vizStyle = vizStyle;
		this.defaultAttrs = defaultAttrs;
	}
	

	/**
	 * Sets all the default Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setDefaults() {
		LOGGER.info("Setting the Default values for Visual Style...");
		/*
		 * for each entry in defaultAttrs
		 * 		Pair p = convertAttribute(getKey(), getValue());
		 * 		VP = p.left()
		 * 		val = p.right()
		 * 		vizStyle.setDefaultValue( VP, val);
		 */
		String colorScheme = defaultAttrs.get("colorscheme");
		for (Entry<String, String> attrEntry : defaultAttrs.entrySet()) {
			String attrKey = attrEntry.getKey();
			String attrVal = attrEntry.getValue();
			LOGGER.info(
				String.format("Converting DOT attribute: %s", attrKey)
			);

			/*
			 * label attribute may be a special case if label="\N".
			 * In dot, \N is an escape sequence that maps the node name
			 * to the node label. So setDefaults needs to run additional code
			 * which should be added to NodeReader to handle the creation of
			 * the mapping
			 */
			/*
			 * if attrKey is "label"

			 *   call handleLabelDefault()
			 *   Method will written differently for each subclass
			 *   NodeReader will create a passthrough mapping if label="\N"
			 *   Every other subclass will return immediately
			 */
			if (attrKey.equals("style")) {
				setStyle(attrVal, vizStyle);
				// this attribute has been handled, move on to next one
				continue;
			}
			if (attrKey.equals("color") || attrKey.equals("fillcolor")
					|| attrKey.equals("fontcolor") || attrKey.equals("bgcolor")) {
				switch (attrKey) {
					case "color": {
						setColor(attrVal, vizStyle, ColorAttribute.COLOR, colorScheme);
						break;
					}
					case "fillcolor": {
						setColor(attrVal, vizStyle, ColorAttribute.FILLCOLOR, colorScheme);
						usedDefaultFillColor = true;
						break;
					}
					case "fontcolor": {
						setColor(attrVal, vizStyle, ColorAttribute.FONTCOLOR, colorScheme);
						break;
					}
					case "bgcolor": {
						setColor(attrVal, vizStyle, ColorAttribute.BGCOLOR, colorScheme);
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
	 * Sets all the bypass Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
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
	 * @param element Graph element that we are getting list of attributes for. Should be
	 * either a Node or an Edge, inputs of any other type will throw an IllegalArgumentException
	 * @return Map<String, String> Where key is attribute name and value is attribute value. Map
	 * contains all attributes in node declaration
	 */
	protected Map<String, String> getAttrMap(Object element) {
	
		 /*
		  * if element instance of Node
		  *		return (Map)((Node)element.getAttributes() )	
		  * if element instanceof Edge
		  * 	sameThing
		  * else
		  * 	throw IllegalArgException
		  */
		if (element instanceof Node) {
			return ((Node) element).getAttributes();
		}
		if (element instanceof Edge) {
			return ((Edge) element).getAttributes();
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * Takes in a dot color string and returns the equivalent Color object
	 * Color formats are:
	 * #RRGGBB (in hex)
	 * #RRGGBBAA (in hex)
	 * H S V (0 <= Hue, saturation, value <= 1.0)
	 * String that is name of color
	 *  
	 * @param color Color from dot file-- takes all color formats
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	protected Color convertColor(String color, String colorScheme) {

		// For testing color file reading
		StringColor strC = new StringColor("svg_colors.txt");
		LOGGER.info("Converting DOT color string to Java Color...");

		//Remove trailing/leading whitespace
		color = color.trim();

		// if color is a list-- will support later. For now, take first color  TODO
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
		LOGGER.info(String.format("Color string: %s", color));
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
		// if color is a string
		else {
			// Read in color name files and find it in there
			StringColor stringColor = new StringColor("svg_colors.txt", "x11_colors.txt");
			Color output = stringColor.getColor(colorScheme, color);

			if(output != null) {
				return output;
			}
			else {
				// If color can't be found return a default color
				return Color.BLUE;
			}
		}
	}
	

	/**
	 * Converts the specified .dot attribute to Cytoscape equivalent
	 * and returns the corresponding VisualProperty and its value
	 * Must be overidden and defined in each sub-class
	 * 
	 * @param name Name of attribute
	 * @param val Value of attribute
	 * 
	 * @return Pair where left value is VisualProperty and right value
	 * is the value of that VisualProperty. VisualProperty corresponds to graphviz
	 * attribute
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Pair<VisualProperty, Object> convertAttribute(String name, String val);

	/**
	 * Converts the "style" attribute from graphviz for default value of Cytoscape
	 * 
	 * @param attrVal String that is the value of "style" 
	 * eg. "dashed, rounded"
	 * @param vizStyle VisualStyle that "style" is being applied to
	 */
	abstract protected void setStyle(String attrVal, VisualStyle vizStyle);

	/**
	 * Converts the "style" attribute from graphviz for bypass value of Cytoscape
	 * 
	 * @param attrVal String that is the value of "style" 
	 * eg. "dashed, rounded"
	 * @param elementView View of element that "style" is being applied to eg. View<CyNode> 
	 */
	abstract protected void setStyle(String attrVal, View<? extends CyIdentifiable> elementView);

	/**
	 * Converts .dot color to Cytoscape default value
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	abstract protected void setColor(String attrVal, VisualStyle vizStyle, ColorAttribute attr, String colorScheme);

	/**
	 * Converts .dot color to Cytoscape bypass value
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param elementView View of element that color is being applied to
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	abstract protected void setColor(String attrVal, View<? extends CyIdentifiable> elementView, ColorAttribute attr, String colorScheme);
}

















