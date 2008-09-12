/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ken Sueda - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.planner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.ITask;

/**
 * @author Ken Sueda
 */
public class TaskPlanContentProvider implements IStructuredContentProvider {

	private final List<AbstractTask> tasks = new ArrayList<AbstractTask>();

	public Object[] getElements(Object inputElement) {
		return tasks.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void addTask(AbstractTask t) {
		if (!tasks.contains(t)) {
			tasks.add(t);
		}
	}

	public void removeTask(ITask t) {
		tasks.remove(t);
	}

	public List<AbstractTask> getTasks() {
		return tasks;
	}
}
