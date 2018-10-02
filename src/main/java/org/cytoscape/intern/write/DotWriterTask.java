/**************************
 * Copyright © 2015-2017 Braxton Fitts, Ziran Zhang, Massoud Maher
 * 
 * This file is part of dot-app.
 * dot-app is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * dot-app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with dot-app.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cytoscape.intern.write;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

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
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task object that writes the network view to a .dot file
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotWriterTask implements CyWriter {

	// whether task is cancelled or not
	private boolean cancelled = false;
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

	// Logger that outputs to Cytoscape standard log file:  .../CytoscapeConfiguration/3/framework-cytoscape.log
	private static final Logger LOGGER = LoggerFactory.getLogger(DotWriterTask.class);

	// whether or not the network view is directed
	private boolean directed = false;

	/*
	 * Tunable to prompt user for edge style curved, normal (segments) or
	 * splines (route around nodes)
	 */
	@Tunable(description = "Pick edge style")
	public ListSingleSelection<String> typer = new ListSingleSelection<String>(
			"Straight segments", "Curved segments",
			"Curved segments routed around nodes");

	/*
	 * Tunable to prompt user for where to put node labels top, bottom, center
	 * or external
	 */
	@Tunable(description = "Pick node label location")
	public ListSingleSelection<String> labelLocations = new ListSingleSelection<String>(
			"Center", "Top", "Bottom", "External");

	/*
	 * Tunable to prompt user for where to put network labels top, bottom, or
	 * none at all
	 */
	@Tunable(description = "Pick network label location")
	public ListSingleSelection<String> networkLabelLocations = new ListSingleSelection<String>(
			"No network label", "Top", "Bottom");

	// whether or not a name had to be modified
	private boolean nameModified = false;

	// value of splines attribute
	private String splinesVal;

	// location of node label
	private String nodeLabelLoc;

	// location of network label
	private String networkLabelLoc;

	// VisualStyle applied to network view
	private VisualStyle vizStyle;

	/**
	 * 
	 * Constructs a DotWriterTask object for exporting network only
	 * 
	 * @param output
	 *            OutputStream that is being written to
	 * @param network
	 *            that is being exported
	 */
	public DotWriterTask(OutputStream output, CyNetwork network) {
		super();
		outputWriter = new OutputStreamWriter(output);
		this.network = network;

		LOGGER.info("DotWriterTask constructed");
	}

	/**
	 * Constructs a DotWriterTask object for exporting network view
	 * 
	 * @param output
	 *            OutputStream that is being written to
	 * @param networkView
	 *            CyNetworkView that is being exported
	 * @param vizMapMgr
	 */
	public DotWriterTask(OutputStream output, CyNetworkView networkView,
			VisualMappingManager vizMapMgr) {

		outputWriter = new OutputStreamWriter(output);
		this.networkView = networkView;
		this.vizStyle = vizMapMgr.getVisualStyle(networkView);
		directed = NetworkPropertyMapper.isDirected(networkView);

		LOGGER.info("DotWriterTask constructed");
	}

	private String buildNodeID(CyNode node) {
		Long nodeSUID = node.getSUID();
		CyNetwork networkModel = networkView.getModel();
		String nodeID = networkModel.getRow(node).get(CyNetwork.NAME,
				String.class);
		nodeID = String.format("%s\247%s", nodeID, nodeSUID);
		nodeID = Mapper.modifyElementID(nodeID);
		if (!nodeID.startsWith("\"") && !nodeID.endsWith("\""))
			nodeID = String.format("\"%s\"", nodeID);
		return nodeID;
	}

	/**
	 * Takes user input for label locations and saves the .dot String value to
	 * instance variables eg. takes Center for nodeLabelLoc and saves it as "c"
	 * for .dot file
	 * 
	 * Precondition: splinesVal, nodeLabelLoc and networkLabelLoc have drop-down
	 * string that user selected Postcondition: splinesVal, nodeLabelLoc and
	 * networkLabelLoc all have their .dot attribute value
	 */
	private void processUserInput() {
		// set splines value
		splinesVal = typer.getSelectedValue();
		LOGGER.info("Raw splinesVal: " + splinesVal);
		switch (splinesVal) {
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
		switch (nodeLabelLoc) {
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
		switch (networkLabelLoc) {
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

	/**
	 * Writes the .dot declaration of each edge to file
	 */
	private void writeEdges() {
		LOGGER.trace("Writing edge declarations...");

		// do the following if user passed in the networkView
		if (networkView != null) {
			// create list of all edge views
			ArrayList<View<CyEdge>> edgeViewList = new ArrayList<View<CyEdge>>(
					networkView.getEdgeViews());
			String edgeType = (directed) ? "->" : "--";

			// for each edge, write declaration string
			for (View<CyEdge> edgeView : edgeViewList) {
				if (!cancelled) {
					edgeMapper = new EdgePropertyMapper(edgeView, vizStyle,
							networkView);

					try {
						// Retrieve source+target node names
						CyEdge edgeModel = edgeView.getModel();
						CyNode sourceNode = edgeModel.getSource();
						CyNode targetNode = edgeModel.getTarget();

						String sourceID = buildNodeID(sourceNode);
						String targetID = buildNodeID(targetNode);

						String edgeName = String.format("%s %s %s", sourceID,
								edgeType, targetID);
						String declaration = String.format("%s %s\n", edgeName,
								edgeMapper.getElementString());

						outputWriter.write(declaration);
					} catch (IOException exception) {
						LOGGER.error("Write failed @ writeEdges()");
					}
				}
				// abort if cancelled
				else {
					return;
				}
			}
		}
		// do the following if user passed in the network
		else {
			List<CyEdge> edgeList = network.getEdgeList();

			for (CyEdge edge : edgeList) {
				if (!cancelled) {
					try {
						CyNode sourceNode = edge.getSource();
						CyNode targetNode = edge.getTarget();

						String sourceID = buildNodeID(sourceNode);
						String targetID = buildNodeID(targetNode);

						String edgeName = String.format("%s %s %s", sourceID,
								"--", targetID);
						String declaration = String.format("%s\n", edgeName);

						outputWriter.write(declaration);
					} catch (IOException exception) {
						LOGGER.error(
								"Write failed @ writeEdges() (passed in network instead of networkView)");
					}
				} else {
					return;
				}
			}
		}
		LOGGER.trace("Finished writing edge declarations...");
	}

	/**
	 * Writes the .dot declaration of each node to file
	 */
	private void writeNodes() {
		LOGGER.info("Writing node declarations...");

		// if the user passed in networkView
		if (networkView != null) {
			// create list of all node views
			ArrayList<View<CyNode>> nodeViewList = new ArrayList<View<CyNode>>(
					networkView.getNodeViews());

			// for each node, write declaration string
			for (View<CyNode> nodeView : nodeViewList) {
				if (!cancelled) {
					nodeMapper = new NodePropertyMapper(nodeView, vizStyle,
							nodeLabelLoc);

					try {
						// Retrieve node name
						CyNode nodeModel = nodeView.getModel();
						String nodeID = buildNodeID(nodeModel);

						String declaration = String.format("%s %s\n", nodeID,
								nodeMapper.getElementString());

						outputWriter.write(declaration);
					} catch (IOException exception) {
						LOGGER.error("Write failed @ writeNodes()");
					}
				} else {
					return;
				}
			}
		}
		// if the user passed in network
		else {
			List<CyNode> nodeList = network.getNodeList();
			for (CyNode node : nodeList) {
				if (!cancelled) {
					try {
						String nodeName = buildNodeID(node);

						String declaration = String.format("%s\n", nodeName);

						outputWriter.write(declaration);
					} catch (IOException exception) {
						LOGGER.error(
							"Write failed @ writeNodes() passed in network "
							+ "instead of networkView"
						);
					}
				}
				// abort if cancelled
				else {
					return;
				}
			}
		}
		LOGGER.info("Finished writing node declarations");
	}

	/**
	 * Writes the network properties to file
	 */
	private void writeProps() {
		try {
			LOGGER.info("Writing network properties...");
			if (network == null) {
				network = networkView.getModel();
			}

			String networkName = network.getRow(network).get(CyNetwork.NAME,
					String.class);
			String networkProps;

			// if we are exporting network view
			if (networkView != null) {
				networkProps = networkMapper.getElementString();
			}
			// if we are only exporting network
			else {
				String moddedName = Mapper.modifyElementID(networkName);
				String label = (networkLabelLoc != null) ? moddedName : "";
				networkProps = String.format("graph %s {\n"
						+ "label = \"%s\"\n" + "splines = \"%s\"\n",
						moddedName, label, splinesVal);
			}
			// if network name was modified
			if (!networkProps.contains(networkName)) {
				nameModified = true;
			}

			outputWriter.write(networkProps);
			LOGGER.info("Finished writing network properties");
		} catch (IOException exception) {
			LOGGER.error("Write failed @ writeProps()");
		}
	}

	/**
	 * Causes the task to stop execution.
	 */
	@Override
	public void cancel() {
		cancelled = true;
	}

	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor
	 *            The TaskMonitor provided by TaskManager to allow the Task to
	 *            modify its user interface.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) {

		taskMonitor.setTitle("Export as GraphViz file");
		taskMonitor.setProgress(-1.0);
		processUserInput();

		if (networkView != null) {
			// constructed here because splinesVal is needed, splinesVal can't
			// be determined until run()
			this.networkMapper = new NetworkPropertyMapper(networkView,
					directed, splinesVal, networkLabelLoc, nodeLabelLoc,
					vizStyle);
		}

		LOGGER.info("Writing .dot file...");
		taskMonitor.setStatusMessage("Writing network attributes...");
		writeProps();
		taskMonitor.setStatusMessage("Writing node declarations...");
		writeNodes();
		taskMonitor.setStatusMessage("Writing edge declarations...");
		writeEdges();

		taskMonitor.setStatusMessage("Closing off file...");
		// Close off file and notify if needed
		try {
			outputWriter.write("}");
			outputWriter.close();
			LOGGER.info("Finished writing file");
			if (nameModified) {
				Notifier.showMessage(
						"Some names have been modified in order to comply to DOT syntax",
						Notifier.MessageType.WARNING);
			} else if (cancelled) {
				Notifier.showMessage("Export cancelled. Be sure to delete the created file",
						Notifier.MessageType.WARNING);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to close file, IOException in DotWriterTask");
		} catch (Exception e) {
			LOGGER.error("Not an IOException");
			throw new RuntimeException(e);
		} finally {
			taskMonitor.setProgress(1.0);
		}
	}
}
