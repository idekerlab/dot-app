package org.cytoscape.intern.read;

import org.cytoscape.intern.FileHandlerManager;

import org.cytoscape.io.CyFileFilter;

import org.cytoscape.io.read.InputStreamTaskFactory;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;

import org.cytoscape.model.subnetwork.CyRootNetworkManager;

import org.cytoscape.view.model.CyNetworkViewFactory;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;

import org.cytoscape.work.TaskIterator;

import java.io.InputStream;
import java.io.IOException;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DotReaderFactory implements InputStreamTaskFactory {

	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.DotReaderFactory");
	
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();

	private CyFileFilter fileFilter;
	private CyNetworkViewFactory netViewFact;
	private CyNetworkFactory netFact;
	private CyNetworkManager netMgr;
	private CyRootNetworkManager rootNetMgr;
	private VisualMappingManager vizMapMgr;
	private VisualStyleFactory vizStyleFact;
	
	public DotReaderFactory(CyFileFilter fileFilter, CyNetworkViewFactory netViewFact,
			CyNetworkFactory netFact, CyNetworkManager netMgr, CyRootNetworkManager rootNetMgr,
			VisualMappingManager vizMapMgr, VisualStyleFactory vizStyleFact) {
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_DotReaderFactory.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);

		this.fileFilter = fileFilter;
		this.netViewFact = netViewFact;
		this.netFact = netFact;
		this.netMgr = netMgr;
		this.rootNetMgr = rootNetMgr;
		this.vizMapMgr = vizMapMgr;
		this.vizStyleFact = vizStyleFact;

	}
	
	@Override
	public CyFileFilter getFileFilter() {
		return fileFilter;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inStream, String inputName) {
		return new TaskIterator(new DotReaderTask(inStream, netViewFact,
				netFact, netMgr, rootNetMgr, vizMapMgr, vizStyleFact));
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
