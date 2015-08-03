package org.cytoscape.intern.read;

import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/*import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.*;*/

public class DotReaderTask extends AbstractCyNetworkReader {
	
	private InputStreamReader inStreamReader;
	
	public DotReaderTask(InputStream inStream, CyNetworkViewFactory cyNetViewFctry,
			CyNetworkFactory cyNetFctry, CyNetworkManager cyNetMgr,
			CyRootNetworkManager cyRootNetMgr) {
		super(inStream, cyNetViewFctry, cyNetFctry, cyNetMgr, cyRootNetMgr);
		inStreamReader = new InputStreamReader(inStream);
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		// TODO Auto-generated method stub
		/*
		 * Steps:
		 * 1. Use Parser to generate Graph objects representing
		 * the graphs from the InputStreamReader
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
		 */

	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
