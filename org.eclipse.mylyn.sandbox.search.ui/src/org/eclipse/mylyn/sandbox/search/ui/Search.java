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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.sandbox.search.ui.DesktopSearchQuery;
import org.eclipse.mylyn.internal.sandbox.search.ui.SearchProviders;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;

/**
 * API for programatic access to search
 * 
 * @author David Green
 * @see NewSearchUI
 */
public class Search {

	/**
	 * Create a search query that can be used with {@link NewSearchUI}.
	 * 
	 * @param criteria
	 *            the search criteria, must not be null
	 * @see NewSearchUI#runQueryInBackground(ISearchQuery)
	 * @see NewSearchUI#runQueryInForeground(org.eclipse.jface.operation.IRunnableContext, ISearchQuery)
	 * @throws CoreException
	 *             if the search query cannot be created
	 * @throws IllegalArgumentException
	 *             if the given criteria is null
	 */
	public static ISearchQuery createSearchQuery(SearchCriteria criteria) throws CoreException {
		if (criteria == null) {
			throw new IllegalArgumentException();
		}
		return new DesktopSearchQuery(SearchProviders.getSearchProvider(), criteria);
	}
}
