package org.cytoscape.intern;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskMonitor;


import java.io.OutputStream;

/**
 * Task factory that creates the file writing task.
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotWriterTask implements CyWriter {
	
	// CyApplicationManager cyAppMgr
	// DataManager dataMgr
	// VisualLexicon visualLex
	// OutputStreamWriter outputWriter
	
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
	
	@Override
	public void cancel() {
		// TODO
	}
	
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
