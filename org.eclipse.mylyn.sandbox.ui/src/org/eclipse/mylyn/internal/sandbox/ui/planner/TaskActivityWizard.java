/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ken Sueda - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.planner;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * @author Ken Sueda
 * @author Mik Kersten
 */
public class TaskActivityWizard extends Wizard implements INewWizard {

	private static final String ID_ACTIVITY_EDITOR = "org.eclipse.mylyn.sandbox.ui.editors.activity";

	private TaskActivityWizardPage planningGamePage;

	public TaskActivityWizard() {
		super();
		init();
		setWindowTitle("New Task Activity Report");
	}

	@Override
	public boolean performFinish() {
		try {
			IWorkbenchPage page = TasksUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (page == null) {
				return false;
			}
			IEditorInput input = new TaskActivityEditorInput(planningGamePage.getReportStartDate(),
					planningGamePage.getReportEndDate(), planningGamePage.getSelectedContainers(),
					TasksUiPlugin.getTaskList());
			page.openEditor(input, ID_ACTIVITY_EDITOR);
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN, "Could not open summary editor", e));
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	private void init() {
		planningGamePage = new TaskActivityWizardPage();
		super.setForcePreviousAndNextButtons(true);
	}

	@Override
	public void addPages() {
		addPage(planningGamePage);
	}
}
