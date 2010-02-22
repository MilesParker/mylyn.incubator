/*******************************************************************************
 * Copyright (c) 2004, 2009 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.commands;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * @author Eugene Kuleshov
 * @since 3.0
 */
// TODO remove dependency on tasks ui
public class ContextCapturePauseHandler extends AbstractHandler //
		implements IElementUpdater {

	private final AbstractContextListener CONTEXT_LISTENER = new AbstractContextListener() {

		@Override
		public void contextActivated(IInteractionContext context) {
			resume();
		}

		@Override
		public void contextDeactivated(IInteractionContext context) {
			resume();
		}
	};

	public ContextCapturePauseHandler() {
		ContextCore.getContextManager().addListener(CONTEXT_LISTENER);
	}

	@Override
	public void dispose() {
		ContextCore.getContextManager().removeListener(CONTEXT_LISTENER);
		super.dispose();
	}

	public Object execute(ExecutionEvent e) throws ExecutionException {
		if (ContextCore.getContextManager().isContextCapturePaused()) {
			resume();
		} else {
			pause();
		}
		return null;
	}

	public void resume() {
		ContextCore.getContextManager().setContextCapturePaused(false);
		if (TaskListView.getFromActivePerspective() != null) {
			TaskListView.getFromActivePerspective().indicatePaused(false);
		}
		refreshCommands();
	}

	public void pause() {
		ContextCore.getContextManager().setContextCapturePaused(true);
		TaskListView.getFromActivePerspective().indicatePaused(true);

		refreshCommands();
	}

	private void refreshCommands() {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		if (service != null) {
			service.refreshElements("org.eclipse.mylyn.tasks.ui.command.previousTask", null);
			service.refreshElements("org.eclipse.mylyn.ui.context.capture.pause.command", null);
		}
	}

	@SuppressWarnings("rawtypes")
	public void updateElement(UIElement element, Map parameters) {
		element.setChecked(ContextCore.getContextManager().isContextCapturePaused());
	}

}