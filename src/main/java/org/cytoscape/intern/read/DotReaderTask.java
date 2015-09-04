package org.cytoscape.intern.read;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.cytoscape.intern.FileHandlerManager;
import org.cytoscape.intern.Notifier;
import org.cytoscape.intern.Notifier.MessageType;
import org.cytoscape.intern.read.reader.NetworkReader;
import org.cytoscape.intern.read.reader.NodeReader;
import org.cytoscape.intern.read.reader.EdgeReader;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskMonitor;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.TokenMgrError;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Id;
import com.alexmerz.graphviz.objects.Node;

/**
 * Task object that reads a GraphViz file into a network/ network view
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotReaderTask extends AbstractCyNetworkReader {
	
	// whether task is cancelled or not
	private boolean cancelled = false;
	
	// debug logger
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.DotReaderTask");
	private FileHandler handler = null;
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();

	// InputStreamReader used as input to the JPGD Parser 
	private InputStreamReader inStreamReader;
	
	// VisualMappingManager to which the new visual style will be added	
	private VisualMappingManager vizMapMgr;
	
	// VisualStyleFactory that will create the VisualStyle for the CyNetwork
	private VisualStyleFactory vizStyleFact;
	
	// Maps the Node in Graph to the CyNode in CyNetwork
	private Map<Node, CyNode> nodeMap;

	// Maps the Node in Graph to the CyNode in CyNetwork
	private Map<Edge, CyEdge> edgeMap;

	// Maps the created CyNetworks to their JPGD Graph object
	private Map<Graph, CyNetwork> graphMap;

	// list of all relevant attributes
	private static final String[] EDGE_ATTRIBUTES = {
		"arrowhead", "arrowtail", "dir"
	};
	private static final String[] NODE_ATTRIBUTES = {
		"height", "width", "shape"
	};
	private static final String[] GRAPH_ATTRIBUTES = {"bgcolor"};
	private static final String[] COMMON_ATTRIBUTES = {
		"color", "fillcolor", "fontcolor", "fontname", "fontsize", "label",
		"penwidth", "pos", "style", "tooltip", "xlabel"
	};
	
	/**
	 * Constructs a DotReaderTask object for importing a dot file
	 * 
	 * @param inStream the stream to be read from
	 * @param netViewFact instance of CyNetworkViewFactory
	 * @param netFact instance of CyNetworkFactory
	 * @param netMgr instance of CyNetworkManager
	 * @param rootNetMgr instance of CyRootNetworkManager
	 * @param vizMapMgr instance of VisualMappingManager
	 * @param vizStyleFact instance of VisualStyleFactory
	 */
	public DotReaderTask(InputStream inStream, CyNetworkViewFactory netViewFact,
			CyNetworkFactory netFact, CyNetworkManager netMgr,
			CyRootNetworkManager rootNetMgr, VisualMappingManager vizMapMgr, VisualStyleFactory vizStyleFact) {
		
		super(inStream, netViewFact, netFact, netMgr, rootNetMgr);
		
		// Make logger write to file
		try {
			handler = new FileHandler("log_DotReaderTask.txt");
			handler.setLevel(Level.ALL);
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);


		// Initialize variables
		inStreamReader = new InputStreamReader(inStream);
		this.vizMapMgr = vizMapMgr;
		this.vizStyleFact = vizStyleFact;
		
		graphMap = new HashMap<Graph, CyNetwork>();
		nodeMap = new HashMap<Node, CyNode>();
		edgeMap = new HashMap<Edge, CyEdge>();
	}

	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor The TaskMonitor provided by TaskManager to allow the
	 * Task to modify its user interface.
	 */
	@Override
	public void run(TaskMonitor monitor) {
		LOGGER.info("Running run() function...");
		monitor.setProgress(0);
		//Initialize the parser
		Parser parser = new Parser();
		
		try {
			
		    LOGGER.info("Begin parsing the input...");
		    monitor.setStatusMessage("Retrieving graph from file...");
			parser.parse(inStreamReader);
			
			// Get list of graphs
			ArrayList<Graph> graphList = parser.getGraphs();
			CyNetwork [] networks = new CyNetwork [graphList.size()];
			
			// Get the root network
			CyRootNetwork root = getRootNetwork();
			
			int networkCounter = 0;
			for (Graph graph : graphList) {
				
				if(!cancelled){
					LOGGER.info("Iterating graph in graphList...");
					CySubNetwork network;
					if (root != null) {
						network = root.addSubNetwork();
					}
					else {
						network = (CySubNetwork)cyNetworkFactory.createNetwork();
					}
				
					// set the name for the network
					String networkName = getGraphName(graph);
					network.getRow(network).set(CyNetwork.NAME, networkName);
				
					// add DOT_network Identifier to Network Table
					monitor.setStatusMessage("Creating table columns...");
					LOGGER.info("Writing DOT_network identifer to Network table...");
					CyTable networkTable = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
					CyTable edgeLocalTable = network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS);
					edgeLocalTable.createColumn("weight", Double.class, false, null);
					networkTable.createColumn("DOT_network", Boolean.class, true);
					networkTable.getRow(network.getSUID()).set("DOT_network", true);
					LOGGER.info(
						String.format("DOT_network identifier written. Result: %s",
							networkTable.getRow(network.getSUID()).get("DOT_network", Boolean.class))
					);
	
					// import nodes
					ArrayList<Node> nodeList = graph.getNodes(true);
					monitor.setStatusMessage("Importing nodes...");
					for (Node node : nodeList) {
						if(!cancelled) {
							importNode(node, network);
						}
						else {
							return;
						}
					}
				
					monitor.setProgress(0.5);
					// import edges
					ArrayList<Edge> edgeList = graph.getEdges();
					monitor.setStatusMessage("Importing edges...");
					for(Edge edge : edgeList) {
						if(!cancelled) {
							importEdge(edge, network);
						}
						else {
							return;
						}
					}
					
					LOGGER.info("All elements imported");
				
					//at the end of each graph iteration, add the created CyNetwork into the CyNetworks array
					networks[networkCounter++] = network;
					LOGGER.info("Network added to array");
				
					//add the graph and the created CyNetwork based on that graph into the graphMap hashmap
					graphMap.put(graph, network);
					LOGGER.info("Graph added to map");
				}
				else {
					// cancel if needed
					return;
				}
				
				monitor.setProgress(1.0);
				this.networks = networks;
				LOGGER.info("CyNetwork objects successfully created");
				FILE_HANDLER_MGR.closeFileHandler(handler);
				LOGGER.removeHandler(handler);
				handler = null;
			}
		}
		catch(ParseException e){
			//Invalid sequence of tokens found in file
			LOGGER.severe(e.getMessage());
			FILE_HANDLER_MGR.closeFileHandler(handler);
			LOGGER.removeHandler(handler);
			handler = null;
			throw new RuntimeException("File did not comply to dot language syntax");
		}
		catch (TokenMgrError e) {
			//Cytoscape is able to continue running even if this error is thrown
			//Invalid token found in file
			LOGGER.severe(e.getMessage());
			FILE_HANDLER_MGR.closeFileHandler(handler);
			LOGGER.removeHandler(handler);
			handler = null;
			throw new RuntimeException("Sorry! File did not comply to dot language syntax");
		}
	}
	
	/**
	 * Causes the task to stop execution.
	 */
	@Override
	public void cancel() {
		cancelled = true;
		super.cancel();
	}
	
	/**
	 * build an instance of CyNetworkView based on the passed in CyNetwork instance
	 * 
	 * @param CyNetwork network from which we want to build the CyNetworkView
	 */
	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		
		// FileHandler for error logging
		if (handler == null) {
			try {
				handler = new FileHandler("log_DotReaderTask.txt");
				handler.setLevel(Level.ALL);
				handler.setFormatter(new SimpleFormatter());
			}
			catch(IOException e) {
				throw new RuntimeException("Logger encountered IO failure");
				// to prevent compiler error
			}
			LOGGER.addHandler(handler);
			FILE_HANDLER_MGR.registerFileHandler(handler);
		}
		LOGGER.info("Executing buildCyNetworkView()...");
		
		// initialize the graph object
		Graph graph = null;
		
		// get JPGD graph object from passed-in network object
		for (Entry<Graph, CyNetwork> entry: graphMap.entrySet()){
			if(network.equals( entry.getValue() )) {
				graph = entry.getKey();
				break;
			}
		}
		
		// error checking if the graph object is not found
		if (graph == null) {
			LOGGER.log(Level.SEVERE, "Graph is null, either it's a empty graph or is not found in HashMap");
			return null;
		}
		
		// Base new VisualStyle off the default style
		VisualStyle defaultVizStyle = vizMapMgr.getDefaultVisualStyle();
		VisualStyle vizStyle = vizStyleFact.createVisualStyle(defaultVizStyle);
		vizStyle.setTitle(
			String.format("%s vizStyle", getGraphName(graph))
		);
		//Enable "Custom Graphics fit to Node" and "Edge color to arrows" dependency
		//Also disable "Lock Node height and width"
		for (VisualPropertyDependency<?> dep : vizStyle.getAllVisualPropertyDependencies()) {
			if (dep.getIdString().equals("nodeCustomGraphicsSizeSync") ||
				dep.getIdString().equals("arrowColorMatchesEdge")) {
				dep.setDependency(true);
			}
			else if (dep.getIdString().equals("nodeSizeLocked")) {
				dep.setDependency(false);
			}

		}
		
		//created a new CyNetworkView based on the cyNetworkViewFactory
		final CyNetworkView networkView = cyNetworkViewFactory.createNetworkView(network);
		

		// initialize readers and begin setting visual properties
		NetworkReader networkReader = new NetworkReader(networkView, vizStyle, getGraphDefaultMap(graph), graph);
		networkReader.setProperties();

		NodeReader nodeReader = new NodeReader(networkView, vizStyle, getNodeDefaultMap(graph), nodeMap);
		nodeReader.setProperties();

		EdgeReader edgeReader = new EdgeReader(networkView, vizStyle, getEdgeDefaultMap(graph), edgeMap);
		edgeReader.setProperties();

		//add the created visualStyle to VisualMappingManager
		vizMapMgr.addVisualStyle(vizStyle);
		
		//return the created cyNetworkView at the end
		LOGGER.finest("Network View created.");
		FILE_HANDLER_MGR.closeFileHandler(handler);
		LOGGER.removeHandler(handler);
		handler = null;
		return networkView;
	}

	/**
	 * Returns array of CyNetworks read
	 * 
	 * @return array of CyNetworks read
	 */
	@Override
	public CyNetwork[] getNetworks() {
		return networks;
	}
	
	/**
	 * Retrieves the name of the graph from its Id Object
	 * @param graph JPGD graph object containing the information
	 * @return name of the graph
	 */
	private String getGraphName(Graph graph) {
		Id graphId = graph.getId();
		String idString = graphId.getId();
		String labelString = graphId.getLabel();
		if (!idString.equals("")) {
			return idString;
		}
		else if (!labelString.equals("")) {
			return labelString;
		}
		return null;
	}
	/**
	 * Retrieves the name of the node from its Id object that will be inserted
	 * into the CyNode table of the CyNetwork
	 * @param node JPGD node object containing the information
	 * @return name of the node
	 */
	private String getNodeName(Node node) {
		Id nodeId = node.getId();
		String idString = nodeId.getId();
		String labelString = nodeId.getLabel();
		if (!idString.equals("")) {
			return idString;
		}
		else if (!labelString.equals("")) {
			String[] parts = labelString.split("§");
			return parts[0];
		}
		return null;
	}
	
	/**
	 * Adds edge into given cytoscape network, sets name and interaction table data.
	 * Also adds edge to edgeMap
	 * 
	 * @param edge Edge that is being added to network
	 * @param network CyNetwork that edge is being added to
	 */
	private void importEdge(Edge edge, CyNetwork network) {

		// get the source and target Nodes from the edge
		Node source = edge.getSource().getNode();
		Node target = edge.getTarget().getNode();
		
		// get the name of both nodes
		String sourceName = getNodeName(source);
		String targetName = getNodeName(target);
		
		// get the CyNode of the source and target node from the hashmap
		CyNode sourceCyNode = nodeMap.get(source);
		CyNode targetCyNode = nodeMap.get(target);
		
		CyEdge cyEdge = null;
		
		// Interaction of the edge
		String interaction;
		/*
		 * if getType returns 2, it's directed, else it's undirected
		 * set the cyEdge and add the cyEdge into the network
		 */
		if (edge.getType() == 2) {
			cyEdge = network.addEdge(sourceCyNode, targetCyNode, true);
			interaction = "interaction";
		}
		else {
			cyEdge = network.addEdge(sourceCyNode, targetCyNode, false);
			interaction = "undirected";
		}
		
		//set the interaction, a attribute of table, to be "interaction"
		network.getDefaultEdgeTable().getRow(cyEdge.getSUID()).set(CyEdge.INTERACTION, interaction);
		
		//set the edge name
		network.getDefaultEdgeTable().getRow(cyEdge.getSUID()).set(CyNetwork.NAME, String.format("%s (%s) %s", sourceName, interaction, targetName));
		
		edgeMap.put(edge, cyEdge);
	}
	
	/**
	 * Adds node into given cytoscape network, sets name.
	 * Also adds node to nodeMap
	 * 
	 * @param node Node being added
	 * @param network CyNetwork it is being added to
	 */
	private void importNode(Node node, CyNetwork network) {
		// add cyNode and set name
		CyNode cyNode = network.addNode();
		String nodeName = getNodeName(node);
		network.getDefaultNodeTable().getRow(cyNode.getSUID()).set(CyNetwork.NAME, nodeName);

		// add the node and the corresponding cyNode into a hashmap for later tracking
		nodeMap.put(node, cyNode);
	}
	
	/**
	 * Returns Map of default attributes and their values for nodes
	 * 
	 * @param graph Graph whose defaults are being returend
	 * @return Map<String, String> where key is attribute name and value
	 * is attribute value
	 */
	private Map<String, String> getNodeDefaultMap(Graph graph) {
		LOGGER.info("Generating the Node Defaults...");
		
		// add each generic Node attribute and value to map
		Map<String, String> output = new HashMap<String, String>();
		for (String commonAttr : COMMON_ATTRIBUTES) {
			LOGGER.info(String.format("Getting default node attribute: %s", commonAttr));
			String val = graph.getGenericNodeAttribute(commonAttr);
			if (val != null) {
				output.put(commonAttr, val);
			}
		}
		for (String nodeAttr : NODE_ATTRIBUTES) {
			LOGGER.info(String.format("Getting default node attribute: %s", nodeAttr));
			String val = graph.getGenericNodeAttribute(nodeAttr);
			if (val != null) {
				output.put(nodeAttr, val);
			}
		}

		return output;
	}

	/**
	 * Returns Map of default attributes and their values for edges
	 * 
	 * @param graph Graph whose defaults are being returend
	 * @return Map<String, String> where key is attribute name and value
	 * is attribute value
	 */
	private Map<String, String> getEdgeDefaultMap(Graph graph) {
		
		Map<String, String> output = new HashMap<String, String>();
		
		// add each generic Edge attribute and value to map
		for (String commonAttr : COMMON_ATTRIBUTES) {
			LOGGER.info(String.format("Getting default edge attribute: %s", commonAttr));
			String val = graph.getGenericEdgeAttribute(commonAttr);
			if (val != null) {
				output.put(commonAttr, val);
			}
		}
		for (String edgeAttr : EDGE_ATTRIBUTES) {
			LOGGER.info(String.format("Getting default edge attribute: %s", edgeAttr));
			String val = graph.getGenericEdgeAttribute(edgeAttr);
			if (val != null) {
				output.put(edgeAttr, val);
			}
		}
		
		return output;
	}
	
	private Map<String, String> getGraphDefaultMap(Graph graph) {
		Map<String, String> output = new HashMap<String, String>();
		
		// add each generic Graph attribute and value to map
		for (String graphAttr : GRAPH_ATTRIBUTES) {
			LOGGER.info(String.format("Getting default graph attribute: %s", graphAttr));
			String val = graph.getGenericGraphAttribute(graphAttr);
			if (val != null) {
				output.put(graphAttr, val);
			}
		}

		return output;
	}	
}






