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
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.eclipse.search.ui.text.TextSearchQueryProvider.TextSearchInput;

/**
 * @author David Green
 */
public class DesktopSearch {

	private static DesktopSearch instance = new DesktopSearch();

	public static DesktopSearch getInstance() {
		return instance;
	}

	public ISearchQuery createQuery(final SearchCriteria item) throws CoreException {
		// FIXME: this is for testing
		TextSearchInput textSearchInput = new TextSearchInput() {

			@Override
			public boolean isRegExSearch() {
				return false;
			}

			@Override
			public boolean isCaseSensitiveSearch() {
				return item.isCaseSensitive();
			}

			@Override
			public String getSearchText() {
				return item.getText();
			}

			@Override
			public FileTextSearchScope getScope() {
				return FileTextSearchScope.newWorkspaceScope(item.getFilenamePatterns(), false);
			}
		};
		return TextSearchQueryProvider.getPreferred().createQuery(textSearchInput);
	}

}
