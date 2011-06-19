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

import org.eclipse.mylyn.sandbox.search.ui.SearchResult;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.SearchResultEvent;

/**
 * @author David Green
 */
@SuppressWarnings("serial")
public class DesktopSearchResultEvent extends SearchResultEvent {

	private final Kind kind;

	private final SearchResult[] items;

	public enum Kind {
		ADDED, CLEARED
	}

	public DesktopSearchResultEvent(ISearchResult searchResult, Kind kind, SearchResult... items) {
		super(searchResult);
		this.kind = kind;
		this.items = items;
	}

	public Kind getKind() {
		return kind;
	}

	public SearchResult[] getItems() {
		return items;
	}
}
