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
/*
 * Created on Oct 14, 2004
 */
package org.eclipse.mylyn.internal.bugzilla.ui.tasklist;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.bugzilla.ui.search.BugzillaSearchEngine;
import org.eclipse.mylyn.internal.bugzilla.ui.search.BugzillaSearchResultCollector;
import org.eclipse.mylyn.internal.bugzilla.ui.search.IBugzillaSearchOperation;
import org.eclipse.mylyn.internal.tasks.ui.search.AbstractRepositorySearchQuery;
import org.eclipse.mylyn.tasks.core.QueryHitCollector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * TODO: delete?
 * 
 * @author Shawn Minto
 */
public class BugzillaCategorySearchOperation extends WorkspaceModifyOperation implements IBugzillaSearchOperation {
	/** The IMember we are doing the search for */

	public interface ICategorySearchListener {
		public void searchCompleted(QueryHitCollector collector);
	}

	/** The bugzilla collector for the search */
	private QueryHitCollector collector = null;

	/** The status of the search operation */
	private IStatus status;

	/** The LoginException that was thrown when trying to do the search */
	private LoginException loginException = null;

	private String queryUrl;

	private TaskRepository repository;

	private int maxHits;

	private boolean isMaxReached;

	/**
	 * Constructor
	 * 
	 * @param m
	 *            The member that we are doing the search for
	 */
	public BugzillaCategorySearchOperation(TaskRepository repository, String queryUrl, int maxHits,
			QueryHitCollector collector) {
		this.queryUrl = queryUrl;
		this.maxHits = maxHits;
		this.repository = repository;
		this.collector = collector;
	}

	@Override
	public void execute(IProgressMonitor monitor) {
		// XXX: Hack
		if (collector instanceof BugzillaSearchResultCollector) {
			((BugzillaSearchResultCollector) collector).setOperation(this);
		}
		collector.setProgressMonitor(monitor);
		Proxy proxySettings = TasksUiPlugin.getDefault().getProxySettings();
		search(queryUrl, proxySettings, monitor);
		for (ICategorySearchListener listener : listeners)
			listener.searchCompleted(collector);
	}

	/**
	 * Perform the actual search on the Bugzilla server
	 * 
	 * @param queryUrl
	 *            The queryUrl to use for the search
	 * @param searchCollector
	 *            The collector to put the search results into
	 * @param monitor
	 *            The progress monitor to use for the search
	 * @return The QueryHitCollector with the search results
	 */
	private QueryHitCollector search(String queryUrl, Proxy proxySettings, IProgressMonitor monitor) {

		// set the initial number of matches to 0
		int matches = 0;
		// setup the progress monitor and start the search
		collector.setProgressMonitor(monitor);
		BugzillaSearchEngine engine = new BugzillaSearchEngine(repository, queryUrl, proxySettings);
		try {
			// perform the search
			status = engine.search(collector, matches, maxHits);

			// check the status so that we don't keep searching if there
			// is a problem
			if (status.getCode() == IStatus.CANCEL) {
				return null;
			} else if (!status.isOK()) {
				return null;
			}
			isMaxReached = engine.isMaxReached();
			return collector;
		} catch (LoginException e) {
			// save this exception to throw later
			this.loginException = e;
		}
		return null;
	}

	/**
	 * @see org.eclipse.mylyn.internal.bugzilla.ui.search.IBugzillaSearchOperation#getStatus()
	 */
	public IStatus getStatus() throws LoginException {
		// if a LoginException was thrown while trying to search, throw this
		if (loginException == null)
			return status;
		else
			throw loginException;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public AbstractRepositorySearchQuery getQuery() {
		return null;
	}

	public void setQuery(AbstractRepositorySearchQuery newQuery) {
	}

	private List<ICategorySearchListener> listeners = new ArrayList<ICategorySearchListener>();

	public void addResultsListener(ICategorySearchListener listener) {
		listeners.add(listener);
	}

	public String getName() {
		return null;
	}

	public boolean isMaxReached() {
		return isMaxReached;
	}
}
