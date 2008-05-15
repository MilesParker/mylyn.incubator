/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.ui.AbstractContextUiBridge;
import org.eclipse.mylyn.context.ui.ContextUi;
import org.eclipse.mylyn.internal.context.ui.ContextUiPlugin;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.sandbox.ui.views.ActiveSearchView;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 * @since 3.0
 */
public class LinkActiveSearchWithEditorAction extends Action {

	public static final String ID = "org.eclipse.mylyn.ui.views.active.search.link";

	private static final String LABEL = "Link with Editor";

	private final SelectionTracker selectionTracker = new SelectionTracker();

	private static LinkActiveSearchWithEditorAction INSTANCE;

	public LinkActiveSearchWithEditorAction() {
		super(LABEL, IAction.AS_CHECK_BOX);
		INSTANCE = this;
		setId(ID);
		setImageDescriptor(CommonImages.LINK_EDITOR);
		setText(LABEL);
		setToolTipText(LABEL);
		ContextUiPlugin.getDefault().getPreferenceStore().setDefault(ID, true);
		update(ContextUiPlugin.getDefault().getPreferenceStore().getBoolean(ID));
	}

	@Override
	public void run() {
		update(isChecked());
	}

	public void update(boolean on) {
		setChecked(on);
		ContextUiPlugin.getDefault().getPreferenceStore().setValue(ID, on);
		ISelectionService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		if (on) {
			service.addPostSelectionListener(selectionTracker);
		} else {
			service.removePostSelectionListener(selectionTracker);
		}
	}

	private static class SelectionTracker implements ISelectionListener {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			try {
				if (selection instanceof TextSelection && part instanceof IEditorPart) {
					ActiveSearchView view = ActiveSearchView.getFromActivePerspective();
					if (view == null || !view.getViewer().getControl().isVisible()) {
						return;
					}
					AbstractContextUiBridge bridge = ContextUi.getUiBridgeForEditor((IEditorPart) part);
					Object toSelect = bridge.getObjectForTextSelection((TextSelection) selection, (IEditorPart) part);
					if (toSelect != null && view.getViewer().testFindItem(toSelect) != null) {
						view.getViewer().setSelection(new StructuredSelection(toSelect), true);
					}
				}
			} catch (Throwable t) {
				StatusHandler.log(new Status(IStatus.ERROR, ContextUiPlugin.ID_PLUGIN,
						"Could not update package explorer", t));
			}
		}
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void runWithEvent(IAction action, Event event) {
		// TODO Auto-generated method stub

	}

	public static LinkActiveSearchWithEditorAction getDefault() {
		return INSTANCE;
	}

	public void propertyChange(PropertyChangeEvent event) {
		// TODO Auto-generated method stub

	}
}
