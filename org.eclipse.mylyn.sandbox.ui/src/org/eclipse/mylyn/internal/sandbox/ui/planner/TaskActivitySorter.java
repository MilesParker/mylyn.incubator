/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.planner;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;

/**
 * @author Ken Sueda
 * @author Mik Kersten
 */
public class TaskActivitySorter extends ViewerSorter {

	public final static int DESCRIPTION = 1;

	public final static int PRIORITY = 2;

	public final static int CREATION_DATE = 3;

	public final static int COMPLETED_DATE = 4;

	public final static int DURATION = 5;

	public final static int ESTIMATED = 6;

	public static final int ICON = 0;

	private final int criteria;

	public TaskActivitySorter(int criteria) {
		super();
		this.criteria = criteria;
	}

	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2) {
		ITask t1 = (ITask) obj1;
		ITask t2 = (ITask) obj2;

		switch (criteria) {
		case DESCRIPTION:
			return compareDescription(t1, t2);
		case PRIORITY:
			return comparePriority(t1, t2);
		case CREATION_DATE:
			return compareCreationDate(t1, t2);
		case COMPLETED_DATE:
			return compareCompletedDate(t1, t2);
		case DURATION:
			return compareDuration(t1, t2);
		case ESTIMATED:
			return compareEstimated(t1, t2);
		default:
			return 0;
		}
	}

	protected int compareDescription(ITask task1, ITask task2) {
		return task1.getSummary().compareToIgnoreCase(task2.getSummary());
	}

	protected int comparePriority(ITask task1, ITask task2) {
		return task1.getPriority().compareTo(task2.getPriority());
	}

	protected int compareCompletedDate(ITask task1, ITask task2) {
		return task2.getCompletionDate().compareTo(task1.getCompletionDate());
	}

	protected int compareEstimated(ITask task1, ITask task2) {
		return task2.getEstimatedTimeHours() - task1.getEstimatedTimeHours();
	}

	protected int compareCreationDate(ITask task1, ITask task2) {
		if (task1.getCreationDate() == null) {
			return 1;
		} else if (task2.getCreationDate() == null) {
			return -1;
		} else {
			return task2.getCreationDate().compareTo(task1.getCreationDate());
		}
	}

	protected int compareDuration(ITask task1, ITask task2) {
		return TasksUiPlugin.getTaskActivityManager().getElapsedTime(task1) < TasksUiPlugin.getTaskActivityManager()
				.getElapsedTime(task2) ? 1 : -1;
	}
}
