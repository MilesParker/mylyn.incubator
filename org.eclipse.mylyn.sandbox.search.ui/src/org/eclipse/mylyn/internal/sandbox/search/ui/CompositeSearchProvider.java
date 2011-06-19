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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.sandbox.search.ui.SearchCallback;
import org.eclipse.mylyn.sandbox.search.ui.SearchCriteria;
import org.eclipse.mylyn.sandbox.search.ui.SearchProvider;
import org.eclipse.osgi.util.NLS;

/**
 * A search provider that delegates to one or more concrete implementations.
 * 
 * @author David Green
 */
public class CompositeSearchProvider extends SearchProvider {

	private List<SearchProvider> delegates;

	public CompositeSearchProvider() {
		this(new ArrayList<SearchProvider>());
	}

	public CompositeSearchProvider(List<SearchProvider> searchProviders) {
		delegates = searchProviders;
	}

	public List<SearchProvider> getDelegates() {
		return delegates;
	}

	public void setDelegates(List<SearchProvider> delegates) {
		this.delegates = delegates;
	}

	@Override
	public void performSearch(SearchCriteria searchSpecification, SearchCallback callback, IProgressMonitor m)
			throws CoreException {
		SubMonitor monitor = SubMonitor.convert(m);
		final int workPerChild = 10000;
		monitor.beginTask(NLS.bind(Messages.CompositeSearchProvider_SearchingTask, searchSpecification.getText()),
				workPerChild * delegates.size());
		try {
			for (SearchProvider provider : delegates) {
				provider.performSearch(searchSpecification, callback, monitor.newChild(workPerChild));
			}
		} finally {
			monitor.done();
		}
	}

}
