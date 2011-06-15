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
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.sandbox.search.ui.DesktopSearchResultEvent.Kind;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;

/**
 * @author David Green
 */
public class DesktopSearchResult implements ISearchResult {

	private List<ISearchResultListener> listeners = new CopyOnWriteArrayList<ISearchResultListener>();

	private List<SearchResultItem> items = new ArrayList<SearchResultItem>();

	private final DesktopSearchQuery searchQuery;

	public DesktopSearchResult(DesktopSearchQuery searchQuery) {
		this.searchQuery = searchQuery;
	}

	public int getSize() {
		synchronized (items) {
			return items.size();
		}
	}

	public void clear() {
		synchronized (items) {
			items.clear();
		}
		fire(Kind.CLEARED);
	}

	public List<SearchResultItem> getItems() {
		synchronized (items) {
			return new ArrayList<SearchResultItem>(items);
		}
	}

	private void fire(Kind eventKind, SearchResultItem... items) {
		DesktopSearchResultEvent event = new DesktopSearchResultEvent(this, eventKind, items);
		for (ISearchResultListener listener : listeners) {
			listener.searchResultChanged(event);
		}
	}

	public void add(SearchResultItem item) {
		synchronized (items) {
			items.add(item);
		}
		fire(Kind.ADDED, item);
	}

	public void addListener(ISearchResultListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ISearchResultListener listener) {
		listeners.remove(listener);
	}

	public String getLabel() {
		return searchQuery.getResultLabel(getSize());
	}

	public String getTooltip() {
		return getLabel();
	}

	public ImageDescriptor getImageDescriptor() {
		return SearchPlugin.getDefault().getImageRegistry().getDescriptor(SearchPlugin.IMAGE_SEARCH);
	}

	public ISearchQuery getQuery() {
		return searchQuery;
	}

}
