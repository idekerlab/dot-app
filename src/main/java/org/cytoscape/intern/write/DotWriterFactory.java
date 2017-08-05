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

import java.io.OutputStream;

import org.cytoscape.intern.Notifier;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task factory that creates the file writing task.
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotWriterFactory implements CyNetworkViewWriterFactory {

	// represents the Graphviz file format
	private CyFileFilter fileFilter;
	
	// contains references to created VisualStyles
	private VisualMappingManager vizMapMgr;
	
	// Logger that outputs to Cytoscape standard log file:  .../CytoscapeConfiguration/3/framework-cytoscape.log
	private static final Logger LOGGER = LoggerFactory.getLogger(DotWriterFactory.class);
	
	/**
	 * Constructs a DotWriterFactory object with a given CyFileFilter
	 * so it knows where to write to file
	 * 
	 * @param fileFilter CyFileFilter associated with this factory
	 * @param vizMapMgr 
	 */
	public DotWriterFactory(CyFileFilter fileFilter, VisualMappingManager vizMapMgr) {

		this.fileFilter = fileFilter;
		this.vizMapMgr = vizMapMgr;
	}

	/**
	 * Returns a task that writes a specified network to a specified stream.
	 * Notifies user that using this option results in large loss of data and recommends
	 * to export with view. Export will only include node and edge declarations
	 * 
	 * @param outStream stream that this writer writes to
	 * @param network CyNetwork that is being written
	 * 
	 * @return CyWriter that writes properties of network parameter to ostream parameter
	 */
	@Override
	public CyWriter createWriter(OutputStream outStream, CyNetwork network) {
		LOGGER.trace("createWriter with CyNetwork param called");
			
		// Notify use they will lose info
		Notifier.showMessage("No visual information will be written to the GraphViz file, only node and edge declarations\n"
				+ "Use File -> Export -> Network and View... instead to maintain visual information", Notifier.MessageType.INFO);
		return new DotWriterTask(outStream, network);
	}
	 
	/**
	 * Creates a task that writes a specified network and its view to a specified stream
	 * 
	 * @param outStream stream that this writer writes to
	 * @param view CyNetworkView that is being written
	 *
	 * @return CyWriter that writes properties of network parameter to ostream parameter
	 */
	@Override
	public CyWriter createWriter(OutputStream outStream, CyNetworkView view) {
		LOGGER.trace("createWriter with CyNetworkView param called");
		return new DotWriterTask(outStream, view, vizMapMgr);
	}
	
	/**
	 * Returns CyFileFilter associated with this factory
	 * 
	 * @return CyFileFilter for this factory
	 */
	@Override
	public CyFileFilter getFileFilter() {
		return fileFilter;
	}
}
