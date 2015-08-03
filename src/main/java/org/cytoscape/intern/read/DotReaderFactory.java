package org.cytoscape.intern.read;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

import java.io.InputStream;

/**
 * Allows the input stream to be set for reader task factories
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */

public class DotReaderFactory implements InputStreamTaskFactory {

	
	//Variable initializations 
	private CyFileFilter fileFilter;
	private CyNetworkViewFactory cyNetViewFctry;
	private CyNetworkFactory cyNetFctry;
	private CyNetworkManager cyNetMgr;
	private CyRootNetworkManager cyRootNetMgr;
	
	
	/**
	 * Sets the DotReaderFactory with associate fileFilter
	 * 
	 * @param fileFilter CyFileFilter associated with this factory
	 */
	public DotReaderFactory(CyFileFilter fileFilter) {
		this.fileFilter = fileFilter;
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
	 * Sets the input stream that will be read by the Reader created from this factory
	 * 
	 * @param inStream The InputStream to be read
	 * @param inputName The name of the input
	 * 
	 * @return TaskIterator created by calling DotReaderTask()
	 */
	@Override
	public TaskIterator createTaskIterator(InputStream inStream, String inputName) {
		return new TaskIterator(new DotReaderTask(inStream, cyNetViewFctry,
				cyNetFctry, cyNetMgr, cyRootNetMgr));
	}

	
	/**
	 * Returns true if the factory is ready to be produce a TaskIterator and false otherwise.
	 * 
	 * @param inStream The InputStream to be read
	 * @param inputName The name of the input
	 * 
	 * @return boolean indicating whether a taskiterator has been created or not
	 */
	@Override
	public boolean isReady(InputStream inStream, String inputName) {
		if (inStream != null && inputName != null) {
			String[] parts = inputName.split(".");
			String extension = parts[parts.length-1];
			if (extension.matches(("gv|dot"))) {
				return true;
			}
		}
		return false;
	}

}
