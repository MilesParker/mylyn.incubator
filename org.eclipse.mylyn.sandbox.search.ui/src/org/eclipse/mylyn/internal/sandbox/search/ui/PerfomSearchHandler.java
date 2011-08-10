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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.mylyn.sandbox.search.ui.Search;
import org.eclipse.mylyn.sandbox.search.ui.SearchCriteria;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;

/**
 * A handler that performs a search.
 * 
 * @author David Green
 */
public class PerfomSearchHandler extends AbstractUiHandler implements IHandler {

	public static final String PARAM_SEARCH_TEXT = "org.eclipse.mylyn.sandbox.search.ui.searchText"; //$NON-NLS-1$

	public static final String PARAM_FILENAME_FILTER = "org.eclipse.mylyn.sandbox.search.ui.filenameFilter"; //$NON-NLS-1$

	@Override
	protected Runnable computeUiRunnable(final ExecutionEvent event) {
		String searchText = event.getParameter(PARAM_SEARCH_TEXT);
		String filenameFilter = event.getParameter(PARAM_FILENAME_FILTER);
		final SearchCriteria criteria = new SearchCriteria();
		criteria.setFilenamePatternsAsText(filenameFilter);
		criteria.setText(searchText);
		return new Runnable() {
			public void run() {
				ISearchQuery searchQuery;
				try {
					searchQuery = Search.createSearchQuery(criteria);
				} catch (CoreException e) {
					ErrorDialog.openError(computeWorkbenchWindow(event).getShell(),
							Messages.DesktopSearchPage_SearchFailedTitle,
							NLS.bind(Messages.DesktopSearchPage_SearchFailedMessage, e.getStatus().getMessage()),
							e.getStatus());
					return;
				}
				NewSearchUI.runQueryInBackground(searchQuery);
			}
		};
	}

}
