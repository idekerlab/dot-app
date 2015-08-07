package org.cytoscape.intern.read;

//In order to access the Reader class that is in the other package
//haven't called the Reader class yet from buildCyNetworkView function
import org.cytoscape.intern.read.reader.Reader; 

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.cytoscape.intern.FileHandlerManager;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.view.vizmap.VisualStyle;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.PortNode;

/**
 * Task object that reads a dot file into a network/ network view
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotReaderTask extends AbstractCyNetworkReader {
	
	// debug logger
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.DotReaderTask");
	private FileHandler handler = null;
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();

	// InputStreamReader used as input to the JPGD Parser 
	private InputStreamReader inStreamReader;
	
	// HashMap that maps the created CyNetworks to their JPGD Graph object
	private HashMap<Graph, CyNetwork> dotGraphs;
	
	// VisualMappingManager to which the new visual style will be added	
	private VisualMappingManager vizMapMgr;
	
	// VisualStyleFactory that will create the VisualStyle for the CyNetwork
	private VisualStyleFactory vizStyleFact;
	
	// HashMap that maps the Node in Graph to the CyNode in CyNetwork
	private HashMap<Node, CyNode> nodeMap;

	
	
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
		dotGraphs = null;
		this.vizMapMgr = vizMapMgr;
		this.vizStyleFact = vizStyleFact;
	}

	
	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor The TaskMonitor provided by TaskManager to allow the
	 * Task to modify its user interface.
	 */
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		// TODO 
		/*
		 * Steps:
		 * 1. Use Parser to generate Graph objects representing
		 * the graphs from the InputStreamReader
		 * 2. Create CyNetwork[] "networks" the size equaling the number of Graph objects
		 * 2. For each graph object do the following:
		 * 		a. Create a CyNetwork
		 * 		b. Set the name of the CyNetwork to the Graph's Id
		 * 		c. Retrieve the list of Node objects
		 * 		d. For each node do the following:
		 * 			- Create a CyNode
		 * 			- Set the name and shared_name of the CyNode to the Node's Id
		 * 		e. For each edge do the following:
		 *			- Create a CyEdge
		 *			- If digraph, set interaction to "interaction", else set to undirected
		 *			- set the name and shared_name of CyEdge to Sourcename (interaction) Targetname
		 * 3. Add CyNetwork to "networks"
		 * 4. Add <CyNetwork, Graph> pair to HashMap
		 */
		/**
		 * Everything we need to do (to help design-- not necessarily in this order):
		 * 1. Use parser to generate Graph objects representing
		 * the graphs from the InputStreamReader
		 * 2. For each graph object do the following:
		 * 		a. Create a CyNetwork
		 * 		b. Set the name of the CyNetwork to the Graph's Id
		 * 		-  Set all network properties
		 * 		[-  Add all CyNodes to network
		 * 		-  set default VPs if exists defaults in .dot file. using getGenericAttriubte()
		 * 		-  If not, use cytoscape defaults (do nothing)
		 * 		-  Set default Visual Properties for nodes
		 * 		-]  Set any bypass VPs
		 * 		-  Do bracketed points for CyEdges. And also:
		 * 			-  If digraph, set interaction to "interaction", else set to undirected
		 * 			-  set the name and shared_name of CyEdge to Sourcename (interaction) Targetname
		 */
		
		LOGGER.info("Running run() function...");
		
		/*******************************************************************************
		 * I believe we don't need to worry about the default VPs stuff in 
		 * run(), but we will need to handle those when we try to build networkView
		 * *****************************************************************************
		 */

		//Initialize the parser
		Parser parser = new Parser();
		
		try{
			
		    LOGGER.info("begin parsing the input...");
		    
		    //parse the passed in input
			parser.parse(inStreamReader);
			
			//graphList holds all the graphs the parser read from the input
			ArrayList <Graph> graphList = parser.getGraphs();
			//initialize a CyNetwork array that holds created CyNetwork from graph
			CyNetwork [] networks = new CyNetwork [graphList.size()];
			//set the counter for the CyNetwork array above
			int networkCounter = 0;
			
			//iterate each graph the parser got from input file
			for (Graph graph : graphList){
				
				LOGGER.info("iterating each graph in graphList...");
				//create a new empty network from cyNetworkFactory
				CyNetwork network = cyNetworkFactory.createNetwork();
				//get the name of the network from the graph
				String networkName = graph.getId().toString(); 
				//set the name for the network
				network.getRow(network).set(CyNetwork.NAME, networkName);
				
				//get all the nodes from the graph, and group them into a nodeList
				ArrayList<Node> nodeList = graph.getNodes(true);
				//iterator each node in the nodeList
				for (Node node : nodeList){
					//initialize a new Cynode into the network
					CyNode cyNode = network.addNode();
					//get the name of the Cynode from the node id 
					String nodeName = node.getId().toString();
					//set the node name for the node
					network.getDefaultNodeTable().getRow(cyNode.getSUID()).set("name", nodeName);
					//add the node and the corresponding cyNode into a hashmap for later tracking
					nodeMap.put(node, cyNode);
				}
				//get all the edges from the graph, and group them into a edgeList
				ArrayList<Edge> edgeList = graph.getEdges();
				//iterate each edge in the edgeList
				for(Edge edge : edgeList){
					//get the source and target portNode from the edge
					PortNode sourcePort = edge.getSource();
					PortNode targetPort = edge.getTarget();
					
					//get the name of both nodes
					String sourceName = sourcePort.getNode().getId().toString();
					String targetName = targetPort.getNode().getId().toString();
					
					//get the CyNode of the source and target node from the hashmap
					CyNode source = nodeMap.get(sourcePort.getNode());
					CyNode target = nodeMap.get(targetPort.getNode());
					//initialize the cyEdge 
					CyEdge cyEdge = null;
					
					//if getType return 2, it's directed, else it's undirected
					//set the cyEdge and add the cyEdge into the network
					if (edge.getType() == 2) {
						cyEdge = network.addEdge(source, target, true);
					}else{
						cyEdge = network.addEdge(source, target, false);
					}
					
					//set the interaction, a attribute of table, to be "interaction"
					network.getDefaultEdgeTable().getRow(cyEdge.getSUID()).set("interaction", "interaction");
					
					//set the edge name
					network.getDefaultEdgeTable().getRow(cyEdge.getSUID()).set("name", sourceName + " (interaction) "+ targetName);
				}
				
				/*********************************************************************************
				 * Not sure whether the ArrayList graphList might contain empty graph (The unsure
				 * code is: ArrayList <Graph> graphList = parser.getGraphs(); at the beginning of 
				 * second try{}), if it's possible for graphList to have empty graph, we probably 
				 * need to check whether network is null(has not been created) before we add it to 
				 * the network array (networks) and before we add it to 
				 * dotGraphs (ArrayList <graph, CyNetwork>)
				 **********************************************************************************
				 */
				
				//at the end of each graph iteration, add the created CyNetwork into the CyNetworks array
				networks[networkCounter++] = network;
				
				//add the graph and the created CyNetwork based on that graph into the dotGraphs hashmap
				dotGraphs.put(graph, network);
								
			}
		}catch(ParseException e){
			//avoid compiling error
			LOGGER.log(Level.SEVERE, "CyNetwork/CyEdge/CyNode initialization failed @ for-each loop in run()");
		}
	}
	
	
	/**
	 * build an instance of CyNetworkView based on the passed in CyNetwork instance
	 * 
	 * @param CyNetwork network from which we want to build the CyNetworkView
	 */
	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		// TODO
		/*
		 * Steps:
		 * Retrieve Graph object corresponding to "arg0" from HashMap
		 * Create new VisualStyle with VisualStyleFactory
		 * Create new CyNetworkView with CyNetworkViewFactory
		 * Pass Graph Object, VisualStyle, CyNetworkView into Reader
		 * in order to set Visual Properties
		 * add VisualStyle to VisualMappingManager
		 * return CyNetworkView
		 */
		
		//codes start at below
		
		LOGGER.info("begin to execute buildCyNetworkView()...");
		
		//initialize the graph object
		Graph graph = null;
		
		//the for loop's purpose below is to get the corresponding graph
		//based on the input network from the hashmap
		for (HashMap.Entry<Graph, CyNetwork> entry: dotGraphs.entrySet()){
			//loop through each entry in hashmap until the corresponding graph is found
			if(network == entry.getValue()){
				graph = entry.getKey();
				break;
			}
		}
		
		//error checking if the graph object is not found
		if (graph == null){
			LOGGER.log(Level.SEVERE, "graph is null, either it's a empty graph or is not found in HashMap");
			return null;
		}
		
		//created a new VisualStyle based on the visualStyleFactory
		VisualStyle visualStyle = vizStyleFact.createVisualStyle("new visual style");
		
		//created a new CyNetworkView based on the cyNetworkViewFactory
		CyNetworkView networkView = cyNetworkViewFactory.createNetworkView(network);
		
		
		/******************************************************************
		 *Somewhere in this method, we need to call the Reader() from 
		 *org.cytoscape.intern.read.reader package (already imported this 
		 *package) by passing in the networkView and visualStyle just 
		 *created above, in order to set all the VPs
		 ******************************************************************
		 */
		
		//Reader.Reader(networkView, visualStyle);
		
		//add the created visualStyle to VisualMappingManager
		vizMapMgr.addVisualStyle(visualStyle);
		
		//return the created cyNetworkView at the end
		return networkView;
	}

	/**
	 * Returns array of CyNetworks read
	 * 
	 * @return array of CyNetworks read
	 */
	public CyNetwork[] getNetworks() {
		// Make array of right size
		CyNetwork[] output = new CyNetwork[dotGraphs.size()];
		
		// fill with CyNetworks
		int i = 0;
		for(Map.Entry<Graph, CyNetwork> entry: dotGraphs.entrySet()) {
			output[i] = entry.getValue();
			i++;
		}
		
		return output;
	}
}







