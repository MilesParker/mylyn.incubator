/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.Person;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListContentProvider;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;

/**
 * @author Rob Elves
 */
public class IncomingTaskListContentProvider extends TaskListContentProvider {

	Set<AbstractTaskContainer> people = new HashSet<AbstractTaskContainer>();

	public IncomingTaskListContentProvider(TaskListView taskListView) {
		super(taskListView);
	}

	@Override
	public Object[] getElements(Object parent) {
		people.clear();
		if (parent instanceof Person) {
			return getChildren(parent);
		} else {
			for (ITaskContainer container : applyFilter(TasksUiPlugin.getTaskList().getRootElements())) {
				for (ITask task : getAllTasks(container.getChildren())) {
					if (task.getOwner() != null && TasksUiInternal.shouldShowIncoming(task)) {
						people.add(new Person(task.getOwner(), task.getConnectorKind(), task.getRepositoryUrl()));
					}
				}
			}
		}
		return people.toArray();
	}

	private Set<ITask> getAllTasks(Collection<ITask> children) {
		if (children != null) {
			Set<ITask> alltasks = new HashSet<ITask>(children);

			for (ITask task : children) {
				if (task instanceof AbstractTask) {
					Set<ITask> childTasks = getAllTasks(((AbstractTask) task).getChildren());
					if (childTasks != null) {
						alltasks.addAll(childTasks);
					}
				}
			}
			return alltasks;
		}
		return null;
	}

	@Override
	public Object[] getChildren(TreePath parentPath) {
		return getChildren(parentPath.getLastSegment());
	}

	@Override
	public Object[] getChildren(Object parent) {
		Set<ITask> children = new HashSet<ITask>();
		if (parent instanceof Person) {
			for (ITaskContainer container : applyFilter(TasksUiPlugin.getTaskList().getRootElements())) {
				for (ITask task : getAllTasks(container.getChildren())) {
					if (task.getOwner() != null && task.getOwner().equals(((Person) parent).getHandleIdentifier())
							&& TasksUiInternal.shouldShowIncoming(task)) {
						children.add(task);
					}
				}
			}
		}
		return children.toArray();
	}

	@Override
	public boolean hasChildren(Object parent) {
		return parent instanceof Person;
	}
}
