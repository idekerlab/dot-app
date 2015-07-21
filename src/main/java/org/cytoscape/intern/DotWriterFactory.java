package org.cytoscape.intern;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Task factory that creates the file writing task.
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotWriterFactory implements CyNetworkViewWriterFactory {
	
	private CyFileFilter fileFilter;
	private CyNetworkViewFactory netViewFactory;
	
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.DotWriterFactory");
	
	/**
	 * Constructs a DotWriterFactory object with a given CyApplicationManager
	 * 
	 * @param fileFilter CyFileFilter associated with this factory
	 * @param netViewFactory network view factory to create network view needed elsewhere
	 */
	public DotWriterFactory(CyFileFilter fileFilter, CyNetworkViewFactory netViewFactory) {
		super();
		this.fileFilter = fileFilter;
		this.netViewFactory = netViewFactory;
		
		// make logger write to file
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_DotWriterFactory.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
	}

	/**
	 * Returns CyFileFilter associated with this factory-- must be overridden
	 * 
	 * @return CyFileFilter for this factory
	 */
	@Override
	public CyFileFilter getFileFilter() {
		return fileFilter;
	}
	 
	/**
	 * Creates a task that writes a specified network to a specified stream.
	 * Notifies user that using this option results in large loss of data and recommends
	 * to export with view
	 * 
	 * @param outStream stream that this writer writes to
	 * @param network CyNetwork that is being written
	 * 
	 * @return CyWriter that writes properties of network parameter to ostream parameter
	 */
	@Override
	public CyWriter createWriter(OutputStream outStream, CyNetwork network) {
		LOGGER.info("createWriter with CyNetwork param called");
			
		// Notify use they will lose info
		
		// maybe we should just return null 
		Notifier.showMessage("No visual information will be written to the GraphViz file, only node and edge declarations\n"
				+ "Use File -> Export -> Network and View... instead to maintain visual information", Notifier.MessageType.INFO);
		return new DotWriterTask(outStream, network);
	}
	
	/**
	 * Creates a task that writes a specified network to a specified stream
	 * 
	 * @param outStream stream that this writer writes to
	 * @param view CyNetworkView that is being written
	 *
	 * @return CyWriter that writes properties of network parameter to ostream parameter
	 */
	@Override
	public CyWriter createWriter(OutputStream outStream, CyNetworkView view) {
		LOGGER.info("createWriter with CyNetworkView param called");
		return new DotWriterTask(outStream, view);
	}
}