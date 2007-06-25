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

package org.eclipse.mylyn.internal.web.tasks;

import org.eclipse.mylyn.tasks.core.AbstractTask;

/**
 * Task used with generic web-based repositories
 * 
 * @author Eugene Kuleshov
 */
public class WebTask extends AbstractTask {

	private static final String UNKNOWN_OWNER = "<unknown>";

	private final String taskPrefix;

	private final String repsitoryType;

	public WebTask(String id, String label, String taskPrefix, String repositoryUrl, String repsitoryType) {
		super(repositoryUrl, id, label);
		this.taskPrefix = taskPrefix;
		this.repsitoryType = repsitoryType;
		setUrl(taskPrefix + id);
	}

	public String getTaskPrefix() {
		return this.taskPrefix;
	}

	@Override
	public String getConnectorKind() {
		return repsitoryType;
	}

	@Override
	public String getOwner() {
		return UNKNOWN_OWNER;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

}
