/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.tasks.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.mylar.internal.tasks.core.WebTask;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.TaskList;

/**
 * Represents pattern-based query on repository web page
 *
 * @author Eugene Kuleshov
 */
public class WebQuery extends AbstractRepositoryQuery {

	private final String taskPrefix;
	private final String queryPattern;
	private final Map<String, String> params;

	public WebQuery(TaskList taskList, String description,
			String queryUrl, String queryPattern, String taskPrefix,
			String repositoryUrl, Map<String, String> params) {
		super(description, taskList);

		this.queryPattern = queryPattern;
		this.taskPrefix = taskPrefix;
		this.params = params;

		setUrl(queryUrl);
		setRepositoryUrl(repositoryUrl);
	}

	@Override
	public String getRepositoryKind() {
		return WebTask.REPOSITORY_TYPE;
	}

	public String getTaskPrefix() {
		return this.taskPrefix;
	}

	public String getQueryPattern() {
		return this.queryPattern;
	}

	public Map<String, String> getQueryParameters() {
		return new LinkedHashMap<String, String>(this.params);
	}

}

