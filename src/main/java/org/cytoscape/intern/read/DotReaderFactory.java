package org.cytoscape.intern.read;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.work.TaskIterator;

public class DotReaderFactory implements InputStreamTaskFactory {

	private CyFileFilter fileFilter;
	
	public DotReaderFactory(CyFileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	@Override
	public CyFileFilter getFileFilter() {
		return fileFilter;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inStream, String inputName) {
		// TODO Auto-generated method stub
		// Will create a single DotReaderTask object, passing in the input stream and input name
		return null;
	}

	@Override
	public boolean isReady(InputStream inStream, String inputName) {
		
		// 
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
