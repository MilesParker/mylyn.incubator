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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.Page;

/**
 * 
 * @author David Green
 */
public class DesktopSearchResultPage extends Page implements ISearchResultPage {

	private String id;
	private DesktopSearchResult searchResult;
	private TreeViewer viewer;
	private ISearchResultViewPart viewPart;
	private Control control;
	private ISearchResultListener listener;

	public Object getUIState() {
		return viewer==null?null:viewer.getSelection();
	}

	public void setInput(ISearchResult search, Object uiState) {
		if (listener == null) {
			listener = new ISearchResultListener() {
				public void searchResultChanged(SearchResultEvent e) {
					DesktopSearchResultEvent event = (DesktopSearchResultEvent) e;
					changed(event);
				}
			};
		}
		if (searchResult != null) {
			searchResult.removeListener(listener);
		}
		searchResult = (DesktopSearchResult) search;
		if (searchResult != null) {
			searchResult.addListener(listener);
		}
		viewer.setInput(searchResult);
		if (uiState instanceof ISelection) {
			viewer.setSelection((ISelection) uiState);
		}

		getViewPart().updateLabel();
	}


	private void changed(DesktopSearchResultEvent event) {
		final Control control = getControl();
		if (!control.isDisposed()) {
			control.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!control.isDisposed()) {
						getViewPart().updateLabel();
					}
				}
			});
		}
	}

	public void setViewPart(ISearchResultViewPart part) {
		this.viewPart = part;
	}

	public ISearchResultViewPart getViewPart() {
		return viewPart;
	}
	
	public void restoreState(IMemento memento) {
		// no state
	}

	public void saveState(IMemento memento) {
		// no state
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		
		viewer = new TreeViewer(container,SWT.MULTI| SWT.H_SCROLL | SWT.V_SCROLL);
		
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		
		getSite().setSelectionProvider(viewer);
		
		// FIXME: actions, context menu
		
		control = container;
		
		getViewPart().updateLabel();
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		getControl().setFocus();
	}

	public void setID(String id) {
		this.id = id;
	}

	public String getID() {
		return id;
	}

	public String getLabel() {
		if (searchResult != null) {
			return searchResult.getLabel();
		}
		return "";
	}

}
