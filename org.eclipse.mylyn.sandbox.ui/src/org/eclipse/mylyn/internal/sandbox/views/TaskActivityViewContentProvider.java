/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.views;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskDelegate;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.ui.TaskListManager;

/**
 * @author Rob Elves
 * @author Mik Kersten
 */
public class TaskActivityViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	private TaskListManager taskListManager;

	public TaskActivityViewContentProvider(TaskListManager taskActivityManager) {
		this.taskListManager = taskActivityManager;
	}

	public Object[] getElements(Object parent) {
//		Set<ScheduledTaskContainer> ranges = new HashSet<ScheduledTaskContainer>();
//		for (ScheduledTaskContainer container : taskListManager.getDateRanges()) {
//			if (!container.isFuture()) {
//				ranges.add(container);
//			}
//		}
		return taskListManager.getDateRanges().toArray();
	}

	public Object getParent(Object child) {
		if (child instanceof ScheduledTaskDelegate) {
			return ((ScheduledTaskDelegate) child).getDateRangeContainer();
		} else {
			return null;
		}
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof ScheduledTaskContainer) {
			// include all tasks with activity in parent's date range
			ScheduledTaskContainer taskContainer = (ScheduledTaskContainer) parent;
			return getActiveChildren(taskContainer).toArray();
		} else {
			return new Object[0];
		}
	}

	public boolean hasChildren(Object parent) {
		return getChildren(parent).length > 0;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	private Set<AbstractTask> getActiveChildren(ScheduledTaskContainer container) {
		Set<AbstractTask> active = new HashSet<AbstractTask>();
		for (AbstractTask task : taskListManager.getTaskList().getAllTasks()) {
			long elapsed = taskListManager.getElapsedTime(task, container.getStart(), container.getEnd());
			if (elapsed > 0) {
				active.add(new ScheduledTaskDelegate(container, task, container.getStart(), container.getEnd(), elapsed));
			}
		}
		return active;
	}
}
