package org.cytoscape.intern;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.view.model.CyNetworkView;

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
	 * Object used to handle the creation of the .dot node and edge declarations
	 */
	DataManager dataMgr;
	
	/**
	 * Object used to write the .dot file
	 */
	OutputStreamWriter outputWriter;
	
	/**
	 * NetworkView being converted to .dot
	 */
	CyNetworkView networkView;
	
	/**
	 * Constructs a DotWriterTask object with a given CyApplicationManager
	 * 
	 * @param output OutputStream that is being written to
	 * @param networkView CyNetworkView that is being exported
	 */
	public DotWriterTask(OutputStream output, CyNetworkView networkView) {
		super();
		
		outputWriter = new OutputStreamWriter(output);
		this.networkView = networkView;
	}

	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor The TaskMonitor provided by TaskManager to allow the
	 * Task to modify its user interface.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) {
		dataMgr = new DataManager();
		writeProps();
		writeNodes();
		writeEdges();
	}
	
	/**
	 * Causes the task to stop execution.
	 */
	@Override
	public void cancel() {
		// TODO
	}
	
	/**
	 * Writes the network properties to file
	 */
	public void writeProps() {
		/**
		 * pseudocode
		 * 
		 * outputWriter.write( dataMgr.getPropertiesString(networkView) );
		 */
	}
	
	/**
	 * Writes the .dot declaration of each node to file
	 */
	public void writeNodes() {
		/**
		 * pseudocode
		 * 
		 * ArrayList< View<CyNode> > nodeViewList = networkView.getNodeViews();
		 * 
		 * for(View<CyNode> nodeView: nodeViewList) {
		 * 		outputWriter.write( dataMgr.getNodeString(nodeView) );
		 * }
		 */
	}
	
	/**
	 * Writes the .dot declaration of each edge
	 */
	public void writeEdges() {
		/**
		 * pseudocode
		 * 
		 * ArrayList< View<CyEdge> > edgeViewList = networkView.getEdgeViews();
		 * 
		 * for(View<CyEdge> edgeView: edgeViewList) {
		 * 		outputWriter.write( dataMgr.getEdgeString(edgeView) );
		 * }
		 */
	}
	
}
