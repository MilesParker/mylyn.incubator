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

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.IContextMenuConstants;
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
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.part.Page;

/**
 * @author David Green
 */
public class DesktopSearchResultPage extends Page implements ISearchResultPage {

	private String id;

	private DesktopSearchResult searchResult;

	private TreeViewer viewer;

	private ISearchResultViewPart viewPart;

	private Control control;

	private ISearchResultListener listener;

	private MenuManager menu;

	private DesktopSearchActionGroup actionGroup;

	public Object getUIState() {
		return viewer == null ? null : viewer.getSelection();
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
		actionGroup = new DesktopSearchActionGroup(part);
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

		viewer = new TreeViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setUseHashlookup(true);

		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new DesktopSearchLabelProvider(new SearchResultLabelProvider()));

		getSite().setSelectionProvider(viewer);

		control = container;

		createMenu();

		viewer.getControl().setMenu(menu.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(getViewPart().getViewSite().getId(), menu, viewer);

		viewer.addOpenListener(new IOpenListener() {

			public void open(OpenEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					OpenFileAction openFileAction = new OpenFileAction(getSite().getPage());
					openFileAction.selectionChanged(structuredSelection);
					if (openFileAction.isEnabled()) {
						openFileAction.run();
					}
				}
			}
		});

		getViewPart().updateLabel();
	}

	private void createMenu() {
		menu = new MenuManager("#PopUp"); //$NON-NLS-1$
		menu.setRemoveAllWhenShown(true);
		menu.setParent(getSite().getActionBars().getMenuManager());
		menu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
	}

	protected void fillContextMenu(IMenuManager menu) {
		createContextMenuGroups(menu);

		actionGroup.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
		actionGroup.fillContextMenu(menu);

		getViewPart().fillContextMenu(menu);
	}

	private void createContextMenuGroups(IMenuManager menu) {
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_OPEN));
		menu.add(new Separator(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_EDIT));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void dispose() {
		if (actionGroup != null) {
			actionGroup.dispose();
			actionGroup = null;
		}
		super.dispose();
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
		return Messages.DesktopSearchResultPage_EmptyLabel;
	}

}
