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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.dialogs.PropertyDialogAction;

class DesktopSearchActionGroup extends ActionGroup {

	private final IViewPart viewPart;

	private final PropertyDialogAction propertyDialogAction;

	private final OpenFileAction openFileAction;

	DesktopSearchActionGroup(IViewPart viewPart) {
		this.viewPart = viewPart;

		propertyDialogAction = new PropertyDialogAction(viewPart.getSite(), viewPart.getSite().getSelectionProvider());

		openFileAction = new OpenFileAction(viewPart.getViewSite().getPage());
	}

	@Override
	public void dispose() {
		propertyDialogAction.dispose();
		super.dispose();
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		ISelection selection = getContext().getSelection();
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			addOpenWithMenu(menu, (IStructuredSelection) selection);
			if (propertyDialogAction.isEnabled()
					&& propertyDialogAction.isApplicableForSelection((IStructuredSelection) selection)) {
				menu.appendToGroup(IContextMenuConstants.GROUP_PROPERTIES, propertyDialogAction);
			}
		}
	}

	private void addOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {
		openFileAction.selectionChanged(selection);
		if (openFileAction.isEnabled()) {
			menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, openFileAction);
		}
		if (selection.size() == 1) {
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IAdaptable) {
				IMenuManager menuManager = new MenuManager(Messages.DesktopSearchActionGroup_MenuOpenWith);

				menuManager.add(new OpenWithMenu(viewPart.getViewSite().getPage(), (IAdaptable) firstElement));

				menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, menuManager);
			}
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertyDialogAction);
	}
}
