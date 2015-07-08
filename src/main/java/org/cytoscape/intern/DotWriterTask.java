package org.cytoscape.intern;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.model.CyNetwork;


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
	// CyApplicationManager cyAppMgr;
	
	/**
	 * Object used to handle the creation of the .dot node and edge declarations
	 */
	DataManager dataMgr;
	
	/**
	 * Object used to write the .dot file
	 */
	OutputStreamWriter outputWriter;
	
	/**
	 * Network being converted to .dot
	 */
	CyNetwork network;
	
	/**
	 * Constructs a DotWriterTask object with a given CyApplicationManager
	 * 
	 * @param cyAppMgr CyApplicationManager used to get network data
	 * @param output OutputStream that is being written to
	 */
	public DotWriterTask(/*CyApplicationManager cyAppMgr,*/ OutputStream output, CyNetwork network) {
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
