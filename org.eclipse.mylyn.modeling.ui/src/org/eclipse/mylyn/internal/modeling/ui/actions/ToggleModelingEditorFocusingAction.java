/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.modeling.ui.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.modeling.ui.ModelingUiPlugin;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author Miles Parker
 * @author Mik Kersten
 */
public class ToggleModelingEditorFocusingAction extends Action implements IWorkbenchWindowActionDelegate,
		IActionDelegate2 {

	public ToggleModelingEditorFocusingAction() {
		setText(Messages.ToggleFocusAction_Focus);
	}

	public void run(IAction action) {
		valueChanged(action, action.isChecked());
	}

	private void valueChanged(IAction action, final boolean on) {
		try {
			action.setChecked(on);
			ModelingUiPlugin.getDefault().getPreferenceStore().setValue(ModelingUiPlugin.FOCUSSING_ENABLED, on);
		} catch (Throwable t) {
			StatusHandler.fail(new Status(IStatus.ERROR, ModelingUiPlugin.ID_PLUGIN,
					"Could not enable editor management", t)); //$NON-NLS-1$
		}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// don't care when the active editor changes
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// don't care when the selection changes
	}

	public void init(IAction action) {
		valueChanged(action,
				ModelingUiPlugin.getDefault().getPreferenceStore().getBoolean(ModelingUiPlugin.FOCUSSING_ENABLED));
	}

	public void dispose() {
		// don't need to do anything

	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void init(IWorkbenchWindow window) {
	}
}
