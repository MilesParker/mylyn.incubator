/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui;

import java.util.TreeMap;

import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.TaskGroup;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListContentProvider;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;

/**
 * @author Eugene Kuleshov
 */
public class GroupedTaskListContentProvider extends TaskListContentProvider {

	public static final String MEMENTO_KEY_GROUP_BY = "groupBy";

	private GroupBy groupBy = GroupBy.None;

	public GroupedTaskListContentProvider(TaskListView taskListView, GroupBy groupBy) {
		super(taskListView);
		this.groupBy = groupBy;
	}

	@Override
	public Object[] getChildren(Object parent) {
		Object[] children = super.getChildren(parent);

		if ((parent instanceof IRepositoryQuery) && groupBy != GroupBy.None) {
			return getGroups((IRepositoryElement) parent, children);
		} else if (parent instanceof TaskGroup) {
			return ((TaskGroup) parent).getChildren().toArray();
		} else {
			return children;
		}
	}

	private TaskGroup[] getGroups(IRepositoryElement parent, Object[] children) {
		TreeMap<String, TaskGroup> groups = new TreeMap<String, TaskGroup>();

		for (Object container : children) {
			if (container instanceof ITask) {
				AbstractTask task = (AbstractTask) container;
				String key = groupBy.getKey(task);
				if (key == null || key.length() == 0) {
					key = "<unknown>";
				}
				TaskGroup group = groups.get(key);
				if (group == null) {
					group = new TaskGroup(parent.getHandleIdentifier(), key, groupBy.name());
					groups.put(key, group);
				}
				group.internalAddChild(task);
			}
		}

		return groups.values().toArray(new TaskGroup[groups.size()]);
	}

	public GroupBy getGroupBy() {
		return groupBy;
	}

}
