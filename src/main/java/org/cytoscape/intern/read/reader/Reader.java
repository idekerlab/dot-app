package org.cytoscape.intern.read.reader;

import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.model.CyNetwork;

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
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.Reader");
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();
	private FileHandler handler = null;

	// view of network being created/modified
	protected CyNetworkView networkView;

	// network being created/modified
	protected CyNetwork network;

	// visualStyle being applied to network, used to set default values
	protected VisualStyle vizStyle;

	// represents .dot graph
	protected Graph graph;
	
	/*
	 * Map of explicitly defined default attributes
	 * key is attribute name, value is value
	 */
	protected Map<String, String> defaultAttrs;
	
	/*
	 * Map of defined bypass attributes
	 * (attributes that are defined at node declaration)
	 */
	protected Map<String, String> bypassAttrs;
	
	// Maps lineStyle attribute values to Cytoscape values
	private static final Map<String, LineType> LINE_TYPE_MAP = null;
	

	/**
	 * Constructs an object of type Reader. Sets up Logger.
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 */
	public Reader(CyNetworkView networkView, VisualStyle vizStyle) {

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
	}
	

	/**
	 * Sets all the default Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
	 */
	private void setDefaults() {
		/*
		 * for each entry in defaultAttrs
		 * 		convertAttribute(getKey(), getValue())
		 */
	}


	/**
	 * Sets all the bypass Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
	 */
	private void setBypasses() {
		/*
		 * for each entry in bypassAttrs
		 * 		convertAttribute(getKey(), getValue())
		 */
	}
	

	/**
	 * Sets all properties, default and bypass for this type of Reader
	 * eg. NodeReader sets all node default and bypass properties
	 * Also sets edgeweights in table for edges
	 */
	public void setAllProperties() {
		setDefaults();
		setBypasses();
	}
	

	/**
	 * Converts the specified .dot attribute to Cytoscape equivalent
	 * by modifying internal data structures like networkView or vizStyle
	 * Must be overidden and defined in each sub-class
	 * 
	 * @param name Name of attribute
	 * @param val Value of attribute
	 * @param isDefault Whether attribute is to be set as default or bypass,
	 * 			doesn't matter for some attributes
	 */
	protected abstract void convertAttribute(String name, String val, boolean isDefault);
}

