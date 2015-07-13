package org.cytoscape.intern;

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
	
	/**
	 * Constructs a DotWriterFactory object with a given CyApplicationManager
	 * 
	 * @param fileFilter CyFileFilter associated with this factory
	 */
	public DotWriterFactory(CyFileFilter fileFilter) {
		super();
		this.fileFilter = fileFilter;
		
		// make logger save to file
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
	 * Creates a task that writes a specified network to a specified stream
	 * 
	 * @param outStream stream that this writer writes to
	 * @param network CyNetwork that is being written
	 * 
	 * @return CyWriter that writes properties of network parameter to ostream parameter
	 */
	@Override
	public CyWriter createWriter(OutputStream outStream, CyNetwork network) {
		/**
		 * Should return null because we are exporting the view data
		 * Maybe not, maybe we have to make both of these do the same thing
		 */
		LOGGER.info("createWriter with CyNetwork param called");
		return null;
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