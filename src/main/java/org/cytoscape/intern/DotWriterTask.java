package org.cytoscape.intern;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.work.TaskMonitor;


import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Task object that writes the network view to a .dot file
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotWriterTask implements CyWriter {
	
	/**
	 * Object used to retrieve the currently selected network view from
	 * the program
	 */
	CyApplicationManager cyAppMgr;
	
	/**
	 * Object used to handle the creation of the .dot node and edge declarations
	 */
	DataManager dataMgr;
	
	/**
	 * Object that contains all the VisualProperty objects associated with each
	 * View object
	 */
	VisualLexicon visualLex;
	
	/**
	 * Object used to write the .dot file
	 */
	OutputStreamWriter outputWriter;
	
	/**
	 * Constructs a DotWriterTask object with a given CyApplicationManager
	 * 
	 * @param cyAppMgr CyApplicationManager used to get network data
	 */
	public DotWriterTask(CyApplicationManager cyAppMgr, OutputStream output) {
		// TODO
		super();
	}

	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor The TaskMonitor provided by TaskManager to allow the
	 * Task to modify its user interface.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) {
		// TODO
	}
	
	/**
	 * Causes the task to stop execution.
	 */
	@Override
	public void cancel() {
		// TODO
	}
	
	/**
	 * Writes the network properties
	 */
	public void writeProps() {
		// TODO
	}
	
	/**
	 * Writes the .dot declaration of each node
	 */
	public void writeNodes() {
		// TODO
	}
	
	/**
	 * Writes the .dot declaration of each edge
	 */
	public void writeEdges() {
		// TODO
	}
	
	/**
	 * Determines if the network view should be treated as a directed graph or
	 * an undirected graph
	 * @return true if the graph should be treated as a directed graph, otherwise false
	 */
	public boolean determineDirected() {
		// TODO
		return false;
	}
	
}
