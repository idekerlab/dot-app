package org.cytoscape.intern.write.mapper;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.cytoscape.intern.FileHandlerManager;
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

	// maps Cytoscape properties  by their ID Strings to their .dot equivalents if relationship is simple equivalency
	protected ArrayList<String> simpleVisPropsToDot; 
	
	// VisualStyle applied to the view
	protected VisualStyle vizStyle;
	
	protected static final int TRANSPARENT = 0x00;
	
	// if node width and height are locked
	private static boolean nodeSizesLockedIsSet = false;
	protected static boolean nodeSizesLocked;
	
	/*
	 * maps Cytoscape line types to the equivalent string used in .dot
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

	// view that this mapper object is mapping
	protected View<? extends CyIdentifiable> view;
	
	// Pixel per inch scaling factor
	protected static final double PPI = 72;

	// debug logger
	protected static final Logger LOGGER;
	protected static FileHandler handler = null;
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();
	// Initialize logger with file handler
	static {
		LOGGER = Logger.getLogger("org.cytoscape.intern.mapper.Mapper");
		
		try {
			handler = new FileHandler("log_Mapper.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);
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
	 * Given a color, returns the color in String format that .dot uses for color.
	 * Format is "#rrggbbaa" -- red, green, blue, alpha in hexadecimal
	 * 
	 * @param color color being converted
	 * @param alpha alpha level of that color-- cytoscape does not use alpha in Paint class
	 * @return String representation of color in .dot format of rgba
	 */
	protected String mapColorToDot(Color color, Integer alpha) {
		LOGGER.info("Creating .dot color attribute string");
		Integer red = color.getRed();
		Integer green = color.getGreen();
		Integer blue = color.getBlue();
		String result = String.format("#%02X%02X%02X%02X", red, green, blue, alpha);
		LOGGER.info("Created .dot color attribute string. Result: " + result);
		return result;
	}
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
		
		LOGGER.info("Label font, size, color, and transparency translation");
		 
		StringBuilder returnValue = null;
		
		if (view.getModel() instanceof CyNode) {
			LOGGER.finest("Mapping font attributes for a node view...");
			LOGGER.info("Determining need for fontname attr");
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
			LOGGER.info("Determining need for fontsize attr");
			if (!isEqualToDefault(size, NODE_LABEL_FONT_SIZE)) {
				String fontSize = String.format("fontsize = \"%d\"", size);
				if (returnValue == null) {
					returnValue = new StringBuilder(fontSize);
				} 
				else {
					returnValue.append(","+fontSize);
				}
			}
			LOGGER.info("Determining need for fontcolor attr");
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
			LOGGER.finest("Mapping font attributes for an edge view...");
			LOGGER.info("Determining need for fontname attr");
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
			LOGGER.info("Determining need for fontsize attr");
			if (!isEqualToDefault(size, EDGE_LABEL_FONT_SIZE)) {
				String fontSize = String.format("fontsize = \"%d\"", size);
				if (returnValue == null) {
					returnValue = new StringBuilder(fontSize);
				} 
				else {
					returnValue.append(","+fontSize);
				}
			}
			LOGGER.info("Determining need for fontcolor attr");
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
			LOGGER.info("Font attributes are defaults");
			return null;
		}
		LOGGER.info("Dot attributes associate with font is: " + returnValue.toString());
		
		return returnValue.toString();		
		
	}
	
	/**
	 * Returns the .dot equivalent in String form for style attribute. Only handles linestyle
	 * Does not include "style=" bit
	 */
	protected String mapDotStyle() {
		LOGGER.info("Building style attr string...");
		StringBuilder dotStyle = null;
		if (view.getModel() instanceof CyNode) {
			LOGGER.finest("Building style string for node view...");
			if (!isEqualToDefault(NODE_BORDER_LINE_TYPE) || !isEqualToDefault(NODE_SHAPE)) {
				LOGGER.info("Not default style attr, building node's own...");
				LineType lineType = view.getVisualProperty(NODE_BORDER_LINE_TYPE);
				NodeShape nodeShape = view.getVisualProperty(NODE_SHAPE);
				String lineStr = "";
				dotStyle = new StringBuilder();

				// get .dot equivalent of line style, see if we need rounded
				lineStr = LINE_TYPE_MAP.get(lineType);
				if (lineStr == null) {
					lineStr = "solid";
					LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
				}
				boolean rounded = nodeShape.equals(ROUND_RECTANGLE);
				String roundedString = (rounded) ? "rounded," : "";
            	String style = String.format("style = \"%s,%sfilled\"", lineStr, roundedString);
            	dotStyle.append(style);
			}
		} 
		else if (view.getModel() instanceof CyEdge) {
			LOGGER.finest("Building style string for edge view...");
			LOGGER.info("Determining need for style attr...");
			if(!isEqualToDefault(EDGE_LINE_TYPE)) {
				LOGGER.info("Not default style attr, building edge's own...");
				LineType lineType = view.getVisualProperty(EDGE_LINE_TYPE);
				String lineStr = LINE_TYPE_MAP.get(lineType);
				dotStyle = new StringBuilder();

				if (lineStr == null) {
					lineStr = "solid";
					LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
				}
				String style = String.format("style = \"%s\"", lineStr);
				dotStyle.append(style);
			}
		}

		if(dotStyle == null) {
			return null;
		}

		return dotStyle.toString();
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
		return String.format("%f,%f", x, -1*y);
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
	/*
	 *  An ID is one of the following:
	 *	Any string of alphabetic ([a-zA-Z\200-\377]) characters, underscores ('_') or digits ([0-9]), not beginning with a digit;
	 *	a numeral [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? );
	 *	any double-quoted string ("...") possibly containing escaped quotes (\");
	 *	an HTML string (<...>). 
	 */
	public static String modifyElementID(String id) {
		LOGGER.info("Preparing to transform ID");
		String alphaNumRegEx = "[a-zA-Z\200-\377_][a-zA-Z\200-\377_0-9]*";
		String numericRegEx = "[-]?([.][0-9]+|[0-9]+([.][0-9]*)?)";
		String quotedRegEx = "\"[^\"]*(\\\")*[^\"]*\"";
		String htmlRegEx = "<.*>";
		if (id.matches(alphaNumRegEx)) {
			LOGGER.info("Passed-in ID is an Alphanumeric ID");
			return id;
		}
		if (id.matches(numericRegEx)) {
			LOGGER.info("Passed-in ID is a Numeric ID");
			return id;
		}
		if (id.matches(quotedRegEx)) {
			LOGGER.info("Passed-in ID is a Quoted ID");
			return id;
		}
		if (id.matches(htmlRegEx)) {
			LOGGER.info("Passed-in ID is an HTML ID");
			return id;
		}
		LOGGER.info("None of the above. Transforming to Quoted ID");
		StringBuilder output = new StringBuilder(id.length() + 2);
		output.append('\"');
		// replace any quotations from name string with escaped quotes
		id = id.replace("\"", "\\\"");
		output.append(id);
		output.append('\"');
		return output.toString();
	}

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
			if((dependency.getIdString()).equals("nodeSizeLocked")) {
				output = dependency.isDependencyEnabled();
			}
		}

		return output;
	}	

	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	public abstract String getElementString();
}
