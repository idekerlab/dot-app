package org.cytoscape.intern;

import org.cytoscape.intern.mapper.NetworkPropertyMapper;
import org.cytoscape.intern.mapper.NodePropertyMapper;
import org.cytoscape.intern.mapper.EdgePropertyMapper;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Task object that writes the network view to a .dot file
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotWriterTask implements CyWriter {
	
	// handles mapping from CS to .dot of respective elements
	private NetworkPropertyMapper networkMapper;
	private NodePropertyMapper nodeMapper;
	private EdgePropertyMapper edgeMapper;
	
	// Object used to write the .dot file
	private OutputStreamWriter outputWriter;
		
	// NetworkView being converted to .dot
	private CyNetworkView networkView;
	
	// debug logger
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.DotWriterTask");
	
	//check whether the task needs to respond to cancellation
	private boolean cancelled = false;
	
	//whether or not the network view is directed
	private boolean directed = false;
	
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
		directed = NetworkPropertyMapper.isDirected(networkView);
		this.networkMapper = new NetworkPropertyMapper(networkView, directed);
		
		// Make logger write to file
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_DotWriterTask.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		
		LOGGER.info("DotWriterTask constructed");
	}

	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor The TaskMonitor provided by TaskManager to allow the
	 * Task to modify its user interface.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) {
		LOGGER.info("Writing .dot file...");
		writeProps();
		writeNodes();
		writeEdges();
		
		try {
			outputWriter.write("}");
			outputWriter.close();
			LOGGER.info("Finished writing file");
		} 
		catch(IOException e) {
			LOGGER.severe("Failed to close file, IOException in DotWriterTask");
		}
		
	}
	
	/**
	 * Causes the task to stop execution.
	 */
	@Override
	public void cancel() {
		// TODO
		
		//set the cancelled to true for further action
		cancelled  = true;
	}
	
	/**
	 * Writes the network properties to file
	 */
	private void writeProps() {
		try {
			LOGGER.info("Writing network properties...");
			outputWriter.write( networkMapper.getElementString() );
			LOGGER.info("Finished writing network properties");
		}
		catch(IOException exception) {
			LOGGER.log(Level.SEVERE, "Write failed @ writeProps()");
		}
	}
	
	/**
	 * Writes the .dot declaration of each node to file
	 */
	private void writeNodes() {
		LOGGER.info("Writing node declarations...");
		
		// create list of all node views
		ArrayList< View<CyNode> > nodeViewList = new ArrayList< View<CyNode> >( networkView.getNodeViews() );
		
		// for each node, write declaration string
		for(View<CyNode> nodeView: nodeViewList) {
	  		nodeMapper = new NodePropertyMapper(nodeView);
	  		
	  		try {
	  			// Retrive node name
	  			CyNode nodeModel = nodeView.getModel();
	  			CyNetwork networkModel = networkView.getModel();
	  			String nodeName = networkModel.getRow(nodeModel).get(CyNetwork.NAME, String.class);
	  			// Remove spaces
	  			nodeName = nodeName.replace(" ", "");
	  			
	  			String declaration = String.format("%s %s\n", nodeName, nodeMapper.getElementString());

	  			outputWriter.write(declaration);
	  		}
	  		catch(IOException exception) {
	  			LOGGER.log(Level.SEVERE, "Write failed @ writeNodes()");
	  		}
		}
		LOGGER.info("Finished writing node declarations");
	}
	
	/**
	 * Writes the .dot declaration of each edge to file
	 */
	private void writeEdges() {
		LOGGER.info("Writing edge declarations...");
		
		// create list of all edge views
		ArrayList< View<CyEdge> > edgeViewList = new ArrayList< View<CyEdge> >( networkView.getEdgeViews() );
		
		String edgeType = (directed) ? "->" : "--";
		
		// for each edge, write declaration string
		for(View<CyEdge> edgeView: edgeViewList) {
	  		edgeMapper = new EdgePropertyMapper(edgeView, networkView);
	  		
	  		try {
	  			// Retrieve source+target node names
	  			CyEdge edgeModel = edgeView.getModel();
	  			CyNetwork networkModel = networkView.getModel();

	  			CyNode sourceNode = edgeModel.getSource();
	  			CyNode targetNode = edgeModel.getTarget();
	  			
	  			String sourceName = networkModel.getRow(sourceNode).get(CyNetwork.NAME, String.class);
	  			// Remove spaces
	  			sourceName = sourceName.replace(" ", "");
	  			
	  			String targetName = networkModel.getRow(targetNode).get(CyNetwork.NAME, String.class);
	  			// Remove spaces
	  			targetName = targetName.replace(" ", "");

	  			String edgeName = String.format("%s %s %s", sourceName, edgeType, targetName);
	  			String declaration = String.format("%s %s\n", edgeName, edgeMapper.getElementString());

	  			outputWriter.write(declaration);
	  		}
	  		catch(IOException exception) {
	  			LOGGER.log(Level.SEVERE, "Write failed @ writeEdges()");
	  		}
		}
		LOGGER.info("Finished writing edge declarations...");
	}
}
