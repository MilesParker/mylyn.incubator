/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.sandbox.search.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

/**
 * 
 * @author David Green
 */
public class DesktopSearchQuery implements ISearchQuery {

	private final SearchProvider provider;
	private final SearchCriteria criteria;
	private DesktopSearchResult searchResult;
	
	public DesktopSearchQuery(SearchProvider provider,SearchCriteria criteria) {
		this.provider = provider;
		this.criteria = criteria;
		searchResult = new DesktopSearchResult(this);
	}

	public IStatus run(IProgressMonitor monitor)
			throws OperationCanceledException {
		searchResult.clear();
		
		SearchCallback callback = new SearchCallback() {
			public void searchResult(SearchResultItem item) {
				searchResult.add(item);
			}
			
			public void searchInitiated() {
			}
			
			public void searchCompleted() {
			}
		};
		try {
			provider.performSearch(criteria, callback, monitor);
		} catch (CoreException e) {
			return e.getStatus();
		}
		
		return Status.OK_STATUS;
	}

	public String getLabel() {
		return "Desktop Search";
	}

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public ISearchResult getSearchResult() {
		return searchResult;
	}

	public String getResultLabel(int size) {
		if (size == 0) {
			return NLS.bind("No files matching \"{0}\"",criteria.getText());
		} else if (size == 1) {
			return NLS.bind("1 file matches \"{0}\"",criteria.getText());
		} else {
			return NLS.bind("{1} files matching \"{0}\"",criteria.getText(),Integer.valueOf(size));
		}
	}

}
