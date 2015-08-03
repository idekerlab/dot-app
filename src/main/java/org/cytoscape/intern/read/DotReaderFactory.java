package org.cytoscape.intern.read;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

import java.io.InputStream;

public class DotReaderFactory implements InputStreamTaskFactory {

	private CyFileFilter fileFilter;
	private CyNetworkViewFactory cyNetViewFctry;
	private CyNetworkFactory cyNetFctry;
	private CyNetworkManager cyNetMgr;
	private CyRootNetworkManager cyRootNetMgr;
	
	public DotReaderFactory(CyFileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	@Override
	public CyFileFilter getFileFilter() {
		return fileFilter;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inStream, String inputName) {
		return new TaskIterator(new DotReaderTask(inStream, cyNetViewFctry,
				cyNetFctry, cyNetMgr, cyRootNetMgr));
	}

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
