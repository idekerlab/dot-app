package org.cytoscape.intern.read.reader;

import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.tuple.Pair;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyIdentifiable;

import org.cytoscape.intern.FileHandlerManager;

import com.alexmerz.graphviz.objects.Graph;

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
	protected FileHandler handler = null;

	// view of network being created/modified
	protected CyNetworkView networkView;

	// network being created/modified
	protected CyNetwork network;

	// visualStyle being applied to network, used to set default values
	protected VisualStyle vizStyle;


	/*
	 * Map of explicitly defined default attributes
	 * key is attribute name, value is value
	 */
	protected Map<String, String> defaultAttrs;
	
	/*
	 * Map of defined bypass attributes
	 * (attributes that are defined at node declaration in file)
	 */
	protected Map<String, String> bypassAttrs;
	
	// Maps lineStyle attribute values to Cytoscape values
	protected static final Map<String, LineType> LINE_TYPE_MAP = null;

	/*
	 * Contains elements of Cytoscape graph and their corresponding JPGD elements
	 * is null for NetworkReader
	 */
	protected Map<Object, CyIdentifiable> elementMap;
	

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

		this.networkView = networkView;
		this.vizStyle = vizStyle;
		this.defaultAttrs = defaultAttrs;
	}
	

	/**
	 * Sets all the default Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
	 */
	private void setDefaults() {
		/*
		 * for each entry in defaultAttrs
		 * 		Pair p = convertAttribute(getKey(), getValue());
		 * 		VP = p.left()
		 * 		val = p.right()
		 * 		vizStyle.setDefaultValue( VP, val);
		 */
	}


	/**
	 * Sets all the bypass Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
	 */
	private void setBypasses() {
		/*
		 * for each entry in elementMap
		 * 		bypassMap = getAttrMap(elementMap.getKey())
		 * 		for each entry in bypassMap
		 * 			Pair p = convertAttribute(name, val);
		 * 			VP = p.left()
		 * 			val = p.right()
		 * 			getValue().setLockedValue( VP, val);	
		 */
	}
	
	/**
	 * Returns the Map of bypass attributes for a given JPGD object.
	 * We define bypass attributes as any attributes declared at the individual
	 * node declaration.
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
		return null;
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
	protected abstract Pair<VisualProperty, Object> convertAttribute(String name, String val);

}

















