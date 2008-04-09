/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionContextListener;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * This action is not persistent, in order to avoid Mylyn not working on startup.
 * 
 * @author Mik Kersten
 */
public class ContextCapturePauseAction extends Action implements IViewActionDelegate, IInteractionContextListener {

	protected IAction initAction = null;

	public void init(IViewPart view) {
		// NOTE: not disposed until shutdown
		ContextCore.getContextManager().addListener(this);
	}

	public void run(IAction action) {
		initAction = action;
		setChecked(!action.isChecked());
		if (isChecked()) {
			resume();
		} else {
			pause();
		}
	}

	public void pause() {
		ContextCore.getContextManager().setContextCapturePaused(true);
		TaskListView.getFromActivePerspective().indicatePaused(true);
	}

	public void resume() {
		ContextCore.getContextManager().setContextCapturePaused(false);
		if (TaskListView.getFromActivePerspective() != null) {
			TaskListView.getFromActivePerspective().indicatePaused(false);
		}
	}

	public void contextActivated(IInteractionContext context) {
		resume();
		setChecked(false);
		if (initAction != null) {
			initAction.setChecked(false);
		}
	}

	public void contextCleared(IInteractionContext context) {
		// ignore
	}

	public void contextDeactivated(IInteractionContext context) {
		// ignore
	}

	public void relationsChanged(IInteractionElement element) {
		// ignore
	}

	public void interestChanged(List<IInteractionElement> elements) {
		// ignore
	}

	public void landmarkAdded(IInteractionElement element) {
		// ignore
	}

	public void landmarkRemoved(IInteractionElement element) {
		// ignore
	}

	public void elementDeleted(IInteractionElement element) {
		// ignore
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// ignore
	}
}
