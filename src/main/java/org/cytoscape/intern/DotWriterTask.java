package org.cytoscape.intern;

import org.cytoscape.intern.mapper.NetworkPropertyMapper;
import org.cytoscape.intern.mapper.NodePropertyMapper;
import org.cytoscape.intern.mapper.EdgePropertyMapper;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.view.model.CyNetworkView;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Task object that writes the network view to a .dot file
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotWriterTask implements CyWriter {
	
	// handles mapping from CS to .dot of respective elements
	NetworkPropertyMapper networkMapper;
	NodePropertyMapper nodeMapper;
	EdgePropertyMapper edgeMapper;
	
	/**
	 * Object used to write the .dot file
	 */
	OutputStreamWriter outputWriter;
	
	/**
	 * NetworkView being converted to .dot
	 */
	CyNetworkView networkView;
	
	/**
	 * Constructs a DotWriterTask object
	 * 
	 * @param output OutputStream that is being written to
	 * @param networkView CyNetworkView that is being exported
	 */
	public DotWriterTask(OutputStream output, CyNetworkView networkView) {
		super();
		
		outputWriter = new OutputStreamWriter(output);
		this.networkView = networkView;
		this.networkMapper = new NetworkPropertyMapper(networkView);
	}

	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor The TaskMonitor provided by TaskManager to allow the
	 * Task to modify its user interface.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) {
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
	private void writeProps() {
		/**
		 * pseudocode
		 * 
		 * outputWriter.write( networkMapper.getElementString(networkView) );
		 */
	}
	
	/**
	 * Writes the .dot declaration of each node to file
	 */
	private void writeNodes() {
		/**
		 * pseudocode
		 * 
		 * ArrayList< View<CyNode> > nodeViewList = networkView.getNodeViews();
		 * 
		 * for(View<CyNode> nodeView: nodeViewList) {
		 * 		nodeMapper = new NodePropertyMapper(nodeView);
		 * 		outputWriter.write( nodeMapper.getElementString() );
		 * }
		 */
	}
	
	/**
	 * Writes the .dot declaration of each edge
	 */
	private void writeEdges() {
		/**
		 * pseudocode
		 * 
		 * ArrayList< View<CyEdge> > edgeViewList = networkView.getEdgeViews();
		 * 
		 * for(View<CyEdge> edgeView: edgeViewList) {
		 * 		edgeMapper = new EdgePropertyMApper(edgeView);
		 * 		outputWriter.write( edgeMapper.getElementString(edgeView) );
		 * }
		 */
	}
	
}
