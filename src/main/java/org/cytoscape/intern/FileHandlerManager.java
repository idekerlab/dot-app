package org.cytoscape.intern;

import java.util.ArrayList;

import java.util.logging.FileHandler;

public class FileHandlerManager {
	private static FileHandlerManager instance = null;
	private ArrayList<FileHandler> fileHandlers = null;
	
	private FileHandlerManager() {
		instance = this;
		fileHandlers = new ArrayList<FileHandler>();
	}
	public static FileHandlerManager getManager() {
		if (instance != null) {
			return instance;
		}
		instance = new FileHandlerManager();
		return instance;
	}
	
	public void registerFileHandler(FileHandler handler) {
		fileHandlers.add(handler);
	}
	public void closeFileHandler(FileHandler handler) {
		if (fileHandlers.contains(handler)) {
			handler.close();
			fileHandlers.remove(handler);
		}
	}
	public void closeAllFileHandlers() {
		for (int i = 0; i < fileHandlers.size(); ++i) {
			FileHandler handler = fileHandlers.get(i);
			fileHandlers.remove(handler);
		}
	}

}
