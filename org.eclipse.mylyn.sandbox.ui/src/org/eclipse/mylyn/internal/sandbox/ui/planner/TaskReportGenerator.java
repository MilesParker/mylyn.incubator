/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;

/**
 * @author Ken Sueda
 * @author Mik Kersten
 * @author Rob Elves (scope report to specific categories and queries)
 */
public class TaskReportGenerator implements IRunnableWithProgress {

	private static final String LABEL_JOB = "Mylyn Task Activity Report";

	private boolean finished;

	private TaskList tasklist = null;

	private final List<ITaskCollector> collectors = new ArrayList<ITaskCollector>();

	private final List<ITask> tasks = new ArrayList<ITask>();

	private final Set<AbstractTaskContainer> filterCategories;

	public TaskReportGenerator(TaskList tlist) {
		this(tlist, null);
	}

	public TaskReportGenerator(TaskList tlist, Set<AbstractTaskContainer> filterCategories) {
		tasklist = tlist;
		this.filterCategories = filterCategories != null ? filterCategories : new HashSet<AbstractTaskContainer>();
	}

	public void addCollector(ITaskCollector collector) {
		collectors.add(collector);
	}

	public void collectTasks() {
		try {
			run(new NullProgressMonitor());
		} catch (InvocationTargetException e) {
			// operation was canceled
		} catch (InterruptedException e) {
			StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN, "Could not collect tasks", e));
		}
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		Set<AbstractTaskContainer> rootElements;
		if (filterCategories.size() == 0) {
			rootElements = tasklist.getRootElements();
		} else {
			rootElements = filterCategories;
		}

		int estimatedItemsToProcess = rootElements.size();
		monitor.beginTask(LABEL_JOB, estimatedItemsToProcess);

		for (Object element : rootElements) {
			monitor.worked(1);
			if (element instanceof ITask) {
				AbstractTask task = (AbstractTask) element;
				for (ITaskCollector collector : collectors) {
					collector.consumeTask(task);
				}
			} else if (element instanceof IRepositoryQuery) {
				// process queries
				RepositoryQuery repositoryQuery = (RepositoryQuery) element;
				for (ITask task : repositoryQuery.getChildren()) {
					for (ITaskCollector collector : collectors) {
						if (task instanceof AbstractTask) {
							collector.consumeTask((AbstractTask) task);
						}
					}
				}
			} else if (element instanceof ITaskContainer) {
				ITaskContainer cat = (ITaskContainer) element;
				for (ITask task : cat.getChildren()) {
					for (ITaskCollector collector : collectors) {
						if (task instanceof AbstractTask) {
							collector.consumeTask((AbstractTask) task);
						}
					}
				}

			}
		}
		// Put the results all into one list (tasks)
		for (ITaskCollector collector : collectors) {
			tasks.addAll(collector.getTasks());
		}
		finished = true;
		monitor.done();
	}

	public List<ITask> getAllCollectedTasks() {
		return tasks;
	}

	public boolean isFinished() {
		return finished;
	}
}
