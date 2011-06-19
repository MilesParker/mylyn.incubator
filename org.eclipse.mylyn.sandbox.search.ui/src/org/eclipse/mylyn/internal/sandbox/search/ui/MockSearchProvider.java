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
package org.eclipse.mylyn.internal.sandbox.search.ui;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.sandbox.search.ui.SearchCallback;
import org.eclipse.mylyn.sandbox.search.ui.SearchCriteria;
import org.eclipse.mylyn.sandbox.search.ui.SearchProvider;
import org.eclipse.mylyn.sandbox.search.ui.SearchResult;

/**
 * Temporary, for testing purposes a {@link SearchProvider} that provides some search results
 * 
 * @author David Green
 */
public class MockSearchProvider extends SearchProvider {

	@Override
	public void performSearch(SearchCriteria searchSpecification, SearchCallback callback, IProgressMonitor monitor)
			throws CoreException {
		int count = 0;
		File[] roots = File.listRoots();
		for (File root : roots) {
			File[] files = root.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						callback.searchResult(new SearchResult(file));
						if (++count > 20) {
							return;
						}
					}
				}
			}
		}
	}

}
