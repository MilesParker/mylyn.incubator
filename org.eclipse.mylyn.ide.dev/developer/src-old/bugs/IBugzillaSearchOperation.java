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

package org.eclipse.mylyn.internal.sandbox.bridge.bugs;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.internal.tasks.ui.search.AbstractRepositorySearchQuery;

/**
 * Interface for the bugzilla search operation
 * 
 * @author Shawn Minto
 * 
 *         TODO: Delete once not requred by sandbox
 */
public interface IBugzillaSearchOperation extends IRunnableWithProgress {

	/**
	 * Get the status of the search operation
	 * 
	 * @return The status of the search operation
	 * @throws LoginException
	 */
	public IStatus getStatus() throws LoginException;

	/**
	 * Get the bugzilla search query
	 * 
	 * @return The bugzilla search query
	 */
	public AbstractRepositorySearchQuery getQuery();

	/**
	 * Sets the bugzilla search query
	 * 
	 * @param newQuery
	 *            The bugzilla search query to be set
	 */
	public void setQuery(AbstractRepositorySearchQuery newQuery);

	public String getName();
}
