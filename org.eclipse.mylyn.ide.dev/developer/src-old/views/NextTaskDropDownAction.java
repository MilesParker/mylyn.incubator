/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.tasks.ui.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.mylyn.internal.tasks.ui.TaskListImages;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskActivationHistory;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;

/**
 * @author Wesley Coelho
 */
public class NextTaskDropDownAction extends TaskNavigateDropDownAction {
	public static final String ID = "org.eclipse.mylyn.tasklist.actions.navigate.next";

	public NextTaskDropDownAction(TaskListView view, TaskActivationHistory history) {
		super(view, history);
		setText("Next Task");
		setToolTipText("Next Task");
		setId(ID);
		setEnabled(false);
		setImageDescriptor(TaskListImages.NAVIGATE_NEXT);
	}

	protected void addActionsToMenu() {
//		List<ITask> tasks = taskHistory.getNextTasks();
//
//		if (tasks.size() > MAX_ITEMS_TO_DISPLAY) {
//			tasks = tasks.subList(0, MAX_ITEMS_TO_DISPLAY);
//		}
//
//		for (int i = 0; i < tasks.size(); i++) {
//			ITask currTask = tasks.get(i);
//			Action taskNavAction = new TaskNavigateAction(currTask);
//			ActionContributionItem item = new ActionContributionItem(taskNavAction);
//			item.fill(dropDownMenu, -1);
//		}
	}

	public void run() {
//		if (taskHistory.hasNext()) {
//			new TaskActivateAction().run(taskHistory.getNextTask());
//			setButtonStatus();
//			view.refreshAndFocus(false);
//		}
	}
}
