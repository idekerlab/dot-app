package org.cytoscape.intern.write;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.cytoscape.intern.FileHandlerManager;
import org.cytoscape.intern.Notifier;
import org.cytoscape.intern.write.mapper.EdgePropertyMapper;
import org.cytoscape.intern.write.mapper.Mapper;
import org.cytoscape.intern.write.mapper.NetworkPropertyMapper;
import org.cytoscape.intern.write.mapper.NodePropertyMapper;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

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
		
	// NetworkView being converted to .dot if view export is selected
	private CyNetworkView networkView = null;
	
	// Network being converted to .dot if network export is selected
	private CyNetwork network = null;
	
	// debug logger
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.DotWriterTask");
	private FileHandler handler;
	
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();
	
	// whether or not the network view is directed
	private boolean directed = false;
	
	/*
	 * Tunable to prompt user for edge style
	 * curved, normal (segments) or splines
	 * (route around nodes)
	 */
	@Tunable(description="Pick edge style")
	public ListSingleSelection<String>  typer = new ListSingleSelection<String>(
			"Straight segments", "Curved segments", "Curved segments routed around nodes");
	
	/*
	 * Tunable to prompt user for where to put node labels
	 * top, bottom, center or external
	 */
	@Tunable(description="Pick node label location")
	public ListSingleSelection<String>  labelLocations = new ListSingleSelection<String>(
			"Center", "Top", "Bottom", "External");
	
	/*
	 * Tunable to prompt user for where to put network labels
	 * top, bottom, or none at all
	 */
	@Tunable(description="Pick network label location")
	public ListSingleSelection<String>  networkLabelLocations = new ListSingleSelection<String>(
			"No network label", "Top", "Bottom");

	// whether or not a name had to be modified
	private boolean nameModified = false;
	
	// value of splines attribute
	private String splinesVal;
	
	// location of node label
	private String nodeLabelLoc;
	
	// location of network label
	private String networkLabelLoc;
	
	/**
	 * Constructs a DotWriterTask object for exporting network view
	 * 
	 * @param output OutputStream that is being written to
	 * @param networkView CyNetworkView that is being exported
	 */
	public DotWriterTask(OutputStream output, CyNetworkView networkView) {
		// Make logger write to file
		handler = null;
		try {
			handler = new FileHandler("log_DotWriterTask.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);
		
		outputWriter = new OutputStreamWriter(output);
		this.networkView = networkView;
		directed = NetworkPropertyMapper.isDirected(networkView);
		
		LOGGER.info("DotWriterTask constructed");
	}
	
	/**
	 * 
	 * Constructs a DotWriterTask object for exporting network only
	 * 
	 * @param output OutputStream that is being written to
	 * @param network that is being exported
	 */
	public DotWriterTask(OutputStream output, CyNetwork network){
		super();
		outputWriter = new OutputStreamWriter(output);
		this.network = network;
		
		// Make logger write to file
		handler = null;
		try {
			handler = new FileHandler("log_DotWriterTask.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);
		
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
		
		processUserInput();
		
		if(networkView != null) {
			// constructed here because splinesVal is needed, splinesVal can't be determined until run()
			this.networkMapper = new NetworkPropertyMapper(networkView, directed, splinesVal, networkLabelLoc);
		}
		
		LOGGER.info("Writing .dot file...");
		taskMonitor.setStatusMessage("Writing network attributes...");
		writeProps();
		taskMonitor.setStatusMessage("Writing node declarations...");
		writeNodes();
		taskMonitor.setStatusMessage("Writing edge declarations...");
		writeEdges();
		
		taskMonitor.setStatusMessage("Closing off file");
		// Close off file and notify if needed
		try {
			outputWriter.write("}");
			outputWriter.close();
			LOGGER.info("Finished writing file");
			if (nameModified) {
				Notifier.showMessage("Some node names have been modified in order to comply to DOT syntax", Notifier.MessageType.WARNING);
			}
		} catch(IOException e) {
			LOGGER.severe("Failed to close file, IOException in DotWriterTask");
		} catch(Exception e) {
			LOGGER.severe("Not an IOException");
			FILE_HANDLER_MGR.closeFileHandler(handler);
			throw new RuntimeException(e);
		} finally {
			FILE_HANDLER_MGR.closeFileHandler(handler);
		}
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
		try {
			LOGGER.info("Writing network properties...");
			if(network == null) {
				network = (CyNetwork)networkView.getModel();
			}
						
			String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
			String networkProps;
			
			// if we are exporting network view
			if(networkView != null){
				networkProps = networkMapper.getElementString();
			}
			// if we are only exporting network
			else {
				networkProps = String.format("graph %s {\nsplines = \"%s\"\n", networkName, splinesVal);
			}
			// if network name was modified
			if (!networkProps.contains(networkName)) {
				nameModified = true;
			}
			
			outputWriter.write( networkProps );
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
		
		// if the user passed in networkView
		if(networkView != null){
			// create list of all node views
			ArrayList< View<CyNode> > nodeViewList = new ArrayList< View<CyNode> >( networkView.getNodeViews() );
		
			// for each node, write declaration string
			for(View<CyNode> nodeView: nodeViewList) {
				nodeMapper = new NodePropertyMapper(nodeView, nodeLabelLoc);
	  		
				try {
					// Retrieve node name
					CyNode nodeModel = nodeView.getModel();
					CyNetwork networkModel = networkView.getModel();
					String nodeName = networkModel.getRow(nodeModel).get(CyNetwork.NAME, String.class);
	  
					String newNodeName = Mapper.modifyElementId(nodeName);
					if (!newNodeName.equals(nodeName)) {
						nameModified = true;
					}

					String declaration = String.format("%s %s\n", newNodeName, nodeMapper.getElementString());

					outputWriter.write(declaration);
				}
				catch(IOException exception) {
					LOGGER.log(Level.SEVERE, "Write failed @ writeNodes()");
				}
			}	
			
		}
		// if the user passed in network
		else {
			List<CyNode> nodeList = network.getNodeList();
			
			for(CyNode node: nodeList){
				try{
					String nodeName = network.getRow(node).get(CyNetwork.NAME,String.class);
					String newNodeName = Mapper.modifyElementId(nodeName);
				
					if(!newNodeName.equals(nodeName)) {
						nameModified = true;
					}
					String declaration = String.format("%s\n", newNodeName);

					outputWriter.write(declaration);
				}
				catch(IOException exception){
					LOGGER.log(Level.SEVERE, "Write failed @ writeNodes() passed in network instead of networkView");
				}
			}
		}
		LOGGER.info("Finished writing node declarations");
	}
	
	/**
	 * Writes the .dot declaration of each edge to file
	 */
	private void writeEdges() {
		LOGGER.info("Writing edge declarations...");
		
		// do the following if user passed in the networkView
		if(networkView != null){
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
					// filter out disallowed chars
					sourceName = Mapper.modifyElementId(sourceName);
	  			
					String targetName = networkModel.getRow(targetNode).get(CyNetwork.NAME, String.class);
					// filter out disallowed chars
					targetName = Mapper.modifyElementId(targetName);

					String edgeName = String.format("%s %s %s", sourceName, edgeType, targetName);
					String declaration = String.format("%s %s\n", edgeName, edgeMapper.getElementString());

					outputWriter.write(declaration);
				}
				catch(IOException exception) {
					LOGGER.log(Level.SEVERE, "Write failed @ writeEdges()");
				}	
			}	
		}
		// do the following if user passed in the network
		else {
			List<CyEdge> edgeList = network.getEdgeList();
			
			for(CyEdge edge : edgeList){
				try{
					CyNode sourceNode = edge.getSource();
					CyNode targetNode = edge.getTarget();
					
					String sourceName = network.getRow(sourceNode).get(CyNetwork.NAME, String.class);
					sourceName = Mapper.modifyElementId(sourceName);
				
					String targetName = network.getRow(targetNode).get(CyNetwork.NAME, String.class);
					targetName = Mapper.modifyElementId(targetName);
				
					String edgeName = String.format("%s %s %s", sourceName, "--", targetName);
					String declaration = String.format("%s\n", edgeName);

					outputWriter.write(declaration);
				}
				catch(IOException exception){
					LOGGER.log(Level.SEVERE, "Write failed @ writeEdges() (passed in network instead of networkView)");
				}
			}
		}
		LOGGER.info("Finished writing edge declarations...");
	}
	
	/**
	 * Takes user input for label locations and saves the .dot String value to instance variables
	 * eg. takes Center for nodeLabelLoc and saves it as "c" for .dot file
	 * 
	 * Precondition: splinesVal, nodeLabelLoc and networkLabelLoc have drop-down string that user selected
	 * Postcondition: splinesVal, nodeLabelLoc and networkLabelLoc all have their .dot attribute value
	 */
	private void processUserInput() {
		// set splines value
		splinesVal = typer.getSelectedValue();
		LOGGER.info("Raw splinesVal: " + splinesVal);
		switch(splinesVal) {
			case "Straight segments":
				splinesVal = "false";
				break;
			case "Curved segments":
				splinesVal = "curved";
				break;	
			case "Curved segments routed around nodes":
				splinesVal = "true";
				break;
		}
		LOGGER.info("Converted splinesVal: " + splinesVal);
		
		// set nodeLabelLocation
		nodeLabelLoc = labelLocations.getSelectedValue();
		LOGGER.info("Raw labelLoc: " + nodeLabelLoc);
		switch(nodeLabelLoc) {
			case "Center":
				nodeLabelLoc = "c";
				break;
			case "Top":
				nodeLabelLoc = "t";
				break;	
			case "Bottom":
				nodeLabelLoc = "b";
				break;
			case "External":
				nodeLabelLoc = "ex";
				break;
		}
		LOGGER.info("Converted labelLoc: " + nodeLabelLoc);
		
		// set networkLabelLocation
		networkLabelLoc = networkLabelLocations.getSelectedValue();
		LOGGER.info("Raw networkLabelLoc: " + networkLabelLoc);
		switch(networkLabelLoc) {
			case "No network label":
				networkLabelLoc = null;
				break;
			case "Top":
				networkLabelLoc = "t";
				break;	
			case "Bottom":
				networkLabelLoc = "b";
				break;
		}
		LOGGER.info("Converted networkLabelLoc: " + networkLabelLoc);
	}
}
