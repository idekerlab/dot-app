package org.cytoscape.intern.read;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/*import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.*;*/

public class DotReaderTask implements CyNetworkReader {
	
	ArrayList<CyNetwork> networks;
	InputStreamReader inputReader;

	public DotReaderTask(InputStream inputStream, String inputName) {
		inputReader = new InputStreamReader(inputStream);
		networks = new ArrayList<CyNetwork>();
	}
	@Override
	public void cancel() {
		// TODO Auto-generated method stub

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
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CyNetwork[] getNetworks() {
		// TODO Auto-generated method stub
		CyNetwork[] networksArray = new CyNetwork[networks.size()];
		return networks.toArray(networksArray);
	}

}
