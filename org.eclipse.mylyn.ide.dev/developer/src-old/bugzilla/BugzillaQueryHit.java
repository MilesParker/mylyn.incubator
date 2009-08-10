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

package org.eclipse.mylyn.internal.bugzilla.core;

import org.eclipse.mylyn.tasks.core.AbstractQueryHit;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.TaskList;

/**
 * @author Ken Sueda
 * @author Mik Kersten (hardening of prototype)
 */
public class BugzillaQueryHit extends AbstractQueryHit {

	private String status;

	public BugzillaQueryHit(TaskList taskList, String description, String priority, String repositoryUrl, String id,
			BugzillaTask task, String status) {
		super(taskList, repositoryUrl, description, id);
		super.priority = priority;
		this.task = task;
		this.status = status;
	}

	@Override
	protected AbstractTask createTask() {		
		return new BugzillaTask(this, true);
	}

	@Override
	public String getUrl() {
		return BugzillaClient.getBugUrlWithoutLogin(repositoryUrl, taskId);
	}

	@Override
	public boolean isCompleted() {
		if (status != null
				&& (status.startsWith("RESO") || status.startsWith("CLO") || status.startsWith("VERI") || status
						.startsWith("FIXED"))) {
			return true;
		}
		return false;
	}
}
