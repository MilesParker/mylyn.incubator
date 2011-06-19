/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.sandbox.search.ui;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author David Green
 */
public class SearchResult extends PlatformObject {

	private final File file;

	public SearchResult(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	/**
	 * get the workspace resource that corresponds to the given {@link #getFile() file}.
	 * 
	 * @return the resource, or null if none is mapped.
	 */
	public IResource getResource() {
		IResource resource = null;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (file.isFile()) {
			resource = workspace.getRoot().getFileForLocation(new Path(file.getName()));
		} else if (file.isDirectory()) {
			resource = workspace.getRoot().getContainerForLocation(new Path(file.getName()));
		}

		return resource;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		if (IResource.class.isAssignableFrom(adapter)) {
			final IResource resource = getResource();
			if (resource != null && adapter.isAssignableFrom(resource.getClass())) {
				return resource;
			}
		}
		return super.getAdapter(adapter);
	}

	@Override
	public String toString() {
		return file.toString();
	}

}
