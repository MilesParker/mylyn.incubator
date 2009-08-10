/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Meghan Allen - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.bugzilla.deprecated;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.deprecated.AbstractLegacyDuplicateDetector;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Meghan Allen
 */
public class BugzillaStackTraceDuplicateDetector extends AbstractLegacyDuplicateDetector {

	private static final int DESCRIPTION_MAX_CHARS = 6000;

	//private static final String NO_STACK_MESSAGE = "Unable to locate a stack trace in the description text.";

	@Override
	public RepositoryQuery getDuplicatesQuery(TaskRepository repository, RepositoryTaskData taskData) {
		String queryUrl = "";
		String searchString = AbstractLegacyDuplicateDetector.getStackTraceFromDescription(taskData.getDescription());
		if (searchString != null && searchString.length() > DESCRIPTION_MAX_CHARS) {
			searchString = searchString.substring(0, DESCRIPTION_MAX_CHARS);
		}

		if (searchString == null) {
			//MessageDialog.openWarning(null, "No Stack Trace Found", NO_STACK_MESSAGE);
			return null;
		}

		try {
			queryUrl = repository.getRepositoryUrl() + "/buglist.cgi?long_desc_type=allwordssubstr&long_desc="
					+ URLEncoder.encode(searchString, repository.getCharacterEncoding());
		} catch (UnsupportedEncodingException e) {
			StatusHandler.log(new Status(IStatus.WARNING, BugzillaCorePlugin.ID_PLUGIN,
					"Error during duplicate detection", e));
			return null;
		}

		queryUrl += "&product=" + taskData.getProduct();

		BugzillaRepositoryQuery bugzillaQuery = new BugzillaRepositoryQuery(repository.getRepositoryUrl(), queryUrl,
				"search");
		return bugzillaQuery;
	}

}
