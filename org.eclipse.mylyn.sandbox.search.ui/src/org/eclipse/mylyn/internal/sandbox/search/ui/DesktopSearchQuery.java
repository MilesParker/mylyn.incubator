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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.sandbox.search.ui.SearchCallback;
import org.eclipse.mylyn.sandbox.search.ui.SearchCriteria;
import org.eclipse.mylyn.sandbox.search.ui.SearchProvider;
import org.eclipse.mylyn.sandbox.search.ui.SearchResult;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

/**
 * @author David Green
 */
public class DesktopSearchQuery implements ISearchQuery {

	private final SearchProvider provider;

	private final SearchCriteria criteria;

	private final DesktopSearchResult searchResult;

	public DesktopSearchQuery(SearchProvider provider, SearchCriteria criteria) {
		this.provider = provider;
		this.criteria = criteria;
		searchResult = new DesktopSearchResult(this);
	}

	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		searchResult.clear();

		SearchCallback callback = new SearchCallback() {
			@Override
			public void searchResult(SearchResult item) {
				searchResult.add(item);
			}

		};
		searchResult.setSearchInProgres(true);
		try {
			provider.performSearch(criteria, callback, monitor);
		} catch (CoreException e) {
			return e.getStatus();
		} finally {
			searchResult.setSearchInProgres(false);
		}

		return Status.OK_STATUS;
	}

	public String getLabel() {
		return Messages.DesktopSearchQuery_Label;
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

	public String getResultLabel(boolean inProgress, int size) {
		if (inProgress) {
			if (size == 0) {
				return NLS.bind(Messages.DesktopSearchQuery_Searching, criteria.getText());
			} else if (size == 1) {
				return NLS.bind(Messages.DesktopSearchQuery_Searching_OneFileMatch, criteria.getText());
			} else {
				return NLS.bind(Messages.DesktopSearchQuery_Searching_NFilesMatching, criteria.getText(),
						Integer.valueOf(size));
			}
		} else {
			if (size == 0) {
				return NLS.bind(Messages.DesktopSearchQuery_NoFilesMatching, criteria.getText());
			} else if (size == 1) {
				return NLS.bind(Messages.DesktopSearchQuery_OneFileMatches, criteria.getText());
			} else {
				if (size >= criteria.getMaximumResults()) {
					NLS.bind(
							Messages.DesktopSearchQuery_NFilesMatching_MaxReached,
							new Object[] { criteria.getText(), Integer.valueOf(size),
									Integer.valueOf(criteria.getMaximumResults()) });
				}
				return NLS.bind(Messages.DesktopSearchQuery_NFilesMatching, criteria.getText(), Integer.valueOf(size));
			}
		}
	}

}
