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

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.sandbox.search.ui.SearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.swt.widgets.Control;

/**
 * @author David Green
 */
class ContentProvider implements ITreeContentProvider, ISearchResultListener {

	private DesktopSearchResult searchResult;

	private TreeViewer viewer;

	public void dispose() {
		if (searchResult != null) {
			searchResult.removeListener(this);
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;
		if (searchResult != null) {
			searchResult.removeListener(this);
		}
		searchResult = (DesktopSearchResult) newInput;
		if (searchResult != null) {
			searchResult.addListener(this);
		}
	}

	public Object[] getElements(Object inputElement) {
		if (searchResult == inputElement) {
			List<SearchResult> items = searchResult.getItems();
			return items.toArray(new Object[items.size()]);
		}
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		if (element != searchResult) {
			return searchResult;
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element == searchResult) {
			return true;
		}
		return false;
	}

	public void searchResultChanged(SearchResultEvent e) {
		final DesktopSearchResultEvent event = (DesktopSearchResultEvent) e;
		final Control control = viewer.getControl();
		if (!control.isDisposed()) {
			control.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!control.isDisposed() && viewer.getInput() == event.getSearchResult()) {
						switch (event.getKind()) {
						case ADDED:
							viewer.add(searchResult, event.getItems());
							break;
						case CLEARED:
							viewer.refresh();
							break;
						}
					}
				}
			});
		}
	}
}
