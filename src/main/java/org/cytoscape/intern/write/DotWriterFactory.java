package org.cytoscape.intern.write;

import org.cytoscape.intern.FileHandlerManager;
import org.cytoscape.intern.Notifier;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

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
	
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.DotWriterFactory");
	
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();
	
	/**
	 * Constructs a DotWriterFactory object with a given CyFileFilter
	 * so it knows where to write to file
	 * 
	 * @param fileFilter CyFileFilter associated with this factory
	 */
	public DotWriterFactory(CyFileFilter fileFilter) {
		super();
		this.fileFilter = fileFilter;
		
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
		FILE_HANDLER_MGR.registerFileHandler(handler);
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
		LOGGER.info("createWriter with CyNetwork param called");
			
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
		LOGGER.info("createWriter with CyNetworkView param called");
		return new DotWriterTask(outStream, view);
	}
}