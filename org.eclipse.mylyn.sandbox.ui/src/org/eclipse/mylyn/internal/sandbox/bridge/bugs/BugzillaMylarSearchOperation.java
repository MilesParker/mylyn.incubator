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
package org.eclipse.mylyn.internal.sandbox.bridge.bugs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaTask;
import org.eclipse.mylyn.internal.bugzilla.ui.tasklist.StackTrace;
import org.eclipse.mylyn.internal.monitor.core.util.StatusManager;
import org.eclipse.mylyn.internal.tasks.ui.TaskFactory;
import org.eclipse.mylyn.internal.tasks.ui.search.AbstractRepositorySearchQuery;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskComment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Bugzilla search operation for Mylar
 * 
 * @author Shawn Minto
 * @author Mik Kersten
 */
public class BugzillaMylarSearchOperation extends WorkspaceModifyOperation implements IBugzillaSearchOperation {

	/** The IMember we are doing the search for */
	private IMember javaElement;

	/** The bugzilla collector for the search */
	private  ProgressQueryHitCollector collector = null;//SearchHitCollector

	/** The status of the search operation */
	private IStatus status;

	/** The LoginException that was thrown when trying to do the search */
	private LoginException loginException = null;

	/** The fully qualified name of the member we are searching for */
	private String name;

	/** The bugzilla search query */
	private AbstractRepositorySearchQuery query;

	private BugzillaMylarSearch search;

	private int scope;

	public BugzillaMylarSearchOperation(BugzillaMylarSearch search, IMember m, int scope) {
		this.javaElement = m;
		this.search = search;
		this.scope = scope;
		name = getFullyQualifiedName(m);
	}

	/**
	 * Get the fully qualified name of a IMember TODO: move to a more central
	 * location so that others can use this, but don't want to add unecessary
	 * coupling
	 * 
	 * @return String representing the fully qualified name
	 */
	public static String getFullyQualifiedName(IJavaElement je) {
		if (!(je instanceof IMember))
			return null;

		IMember m = (IMember) je;
		if (m.getDeclaringType() == null)
			return ((IType) m).getFullyQualifiedName();
		else
			return m.getDeclaringType().getFullyQualifiedName() + "." + m.getElementName();
	}

	@Override
	public void execute(IProgressMonitor monitor) {

		ProgressQueryHitCollector searchCollector = null;

		if (scope == BugzillaMylarSearch.FULLY_QUAL) {
			searchCollector = searchQualified(search.getServerUrl(), monitor);
		} else if (scope == BugzillaMylarSearch.UNQUAL) {
			searchCollector = searchUnqualified(search.getServerUrl(), monitor);
		} else if (scope == BugzillaMylarSearch.LOCAL_QUAL) {
			searchCollector = searchLocalQual(monitor);
		} else if (scope == BugzillaMylarSearch.LOCAL_UNQUAL) {
			searchCollector = searchLocalUnQual(monitor);
		} else {
			status = Status.OK_STATUS;
			return;
		}

		if (searchCollector == null) {
			search.notifySearchCompleted(new ArrayList<BugzillaReportInfo>());
			return;
		}

		Set<AbstractTask> l = searchCollector.getTasks();

		// get the list of doi elements
		List<BugzillaReportInfo> doiList = getDoiList(l);

		// we completed the search, so notify all of the listeners
		// that the search has been completed
		MylarBugsManager.getBridge().addToLandmarksHash(doiList, javaElement, scope);
		search.notifySearchCompleted(doiList);
		// MIK: commmented out logging
		// MonitorPlugin.log(this, "There were " + doiList.size() + " items
		// found");
	}

	/**
	 * Search the local bugs for the member using the qualified name
	 * 
	 * @param monitor
	 *            The progress monitor to search with
	 * @return The QueryHitCollector with the results of the search
	 */
	private ProgressQueryHitCollector searchLocalQual(IProgressMonitor monitor) {

		// get the fully qualified name for searching
		String elementName = getFullyQualifiedName(javaElement);

		// setup the search result collector
		collector = new ProgressQueryHitCollector(TasksUiPlugin.getTaskListManager().getTaskList(), new TaskFactory(null));//SearchHitCollector(TasksUiPlugin.getTaskListManager().getTaskList());
		//collector.setOperation(this);
		collector.setProgressMonitor(monitor);

		// get all of the root tasks and start the search
		Set<AbstractTask> tasks = TasksUiPlugin.getTaskListManager().getTaskList().getDefaultCategory().getChildren();
		searchLocal(tasks, collector, elementName, monitor);
		for (AbstractTaskContainer cat : TasksUiPlugin.getTaskListManager().getTaskList().getTaskContainers()) {
			searchLocal(cat.getChildren(), collector, elementName, monitor);
		}

		// return the collector
		return collector;
	}

	/**
	 * Search the local bugs for the member using the unqualified name
	 * 
	 * @param monitor
	 *            The progress monitor to search with
	 * @return The QueryHitCollector with the results of the search
	 */
	private ProgressQueryHitCollector searchLocalUnQual(IProgressMonitor monitor) {

		// get the element name for searching
		String elementName = javaElement.getElementName();

		// setup the search result collector
		collector = new ProgressQueryHitCollector(TasksUiPlugin.getTaskListManager().getTaskList(), new TaskFactory(null));//SearchHitCollector(TasksUiPlugin.getTaskListManager().getTaskList());
		//collector.setOperation(this);
		collector.setProgressMonitor(monitor);

		// get all of the root tasks and start the search
		Set<AbstractTask> tasks = TasksUiPlugin.getTaskListManager().getTaskList().getDefaultCategory().getChildren();
		searchLocal(tasks, collector, elementName, monitor);
		for (AbstractTaskContainer cat : TasksUiPlugin.getTaskListManager().getTaskList().getTaskContainers()) {
			searchLocal(cat.getChildren(), collector, elementName, monitor);
		}
		// return the collector
		return collector;
	}

	/**
	 * Search the local bugs for the member
	 * 
	 * @param tasks
	 *            The tasks to search
	 * @param searchCollector
	 *            The collector to add the results to
	 * @param elementName
	 *            The name of the element that we are looking for
	 * @param monitor
	 *            The progress monitor
	 */
	private void searchLocal(Set<AbstractTask> tasks, ProgressQueryHitCollector searchCollector, String elementName,
			IProgressMonitor monitor) {
		if (tasks == null)
			return;

		// go through all of the tasks
		for (AbstractTask task : tasks) {
			monitor.worked(1);

			// check what kind of task it is
			if (task instanceof BugzillaTask) {

				// we have a bugzilla task, so get the bug report
				BugzillaTask bugTask = (BugzillaTask) task;
				RepositoryTaskData bugTaskData = TasksUiPlugin.getDefault().getTaskDataManager().getNewTaskData(bugTask.getRepositoryUrl(), bugTask.getTaskId());
				//RepositoryTaskData bugTaskData = bugTask.getTaskData();

				// parse the bug report for the element that we are searching
				// for
				boolean isHit = search(elementName, bugTaskData);

				// determine if we have a hit or not
				if (isHit) {
//					// make a search hit from the bug and then add it to the collector
//					BugzillaQueryHit hit = new BugzillaQueryHit(TasksUiPlugin.getTaskListManager().getTaskList(), bugTaskData.getDescription(), "", bugTaskData.getRepositoryUrl(), bugTaskData.getId(), null, "");
//					BugzillaTask task = new BugzillaTask();
					searchCollector.accept(bugTask);
				}
			}
		}
		status = Status.OK_STATUS;
	}

	/**
	 * Search the bug for the given element name
	 * 
	 * @param elementName
	 *            The name of the element to search for
	 * @param bug
	 *            The bug to search in
	 */
	private boolean search(String elementName, RepositoryTaskData bug) {

		if (bug == null)
			return false; // MIK: added null check here
		String description = bug.getDescription();
		String summary = bug.getSummary();
		List<TaskComment> taskComments = bug.getComments();

		// search the summary and the summary
		if (Util.hasElementName(elementName, summary))
			return true;

		if (Util.hasElementName(elementName, description))
			return true;

		Iterator<TaskComment> comItr = taskComments.iterator();
		while (comItr.hasNext()) {
			TaskComment taskComment = comItr.next();
			String commentText = taskComment.getText();
			// search the text for a reference to the element
			if (Util.hasElementName(elementName, commentText))
				return true;
		}
		return false;
	}

	/**
	 * Perform the actual search on the Bugzilla server
	 * 
	 * @param url
	 *            The url to use for the search
	 * @param searchCollector
	 *            The collector to put the search results into
	 * @param monitor
	 *            The progress monitor to use for the search
	 * @return The QueryHitCollector with the search results
	 */
	private ProgressQueryHitCollector search(String url, TaskRepository repository,
			ProgressQueryHitCollector searchCollector, IProgressMonitor monitor) {

		// set the initial number of matches to 0
		int matches = 0;
		// setup the progress monitor and start the search
		searchCollector.setProgressMonitor(monitor);
		
		BugzillaSearchEngine engine = new BugzillaSearchEngine(repository, url);
		try {
			// perform the search
			status = engine.search(searchCollector, matches);

			// check the status so that we don't keep searching if there
			// is a problem
			if (status.getCode() == IStatus.CANCEL) {
				return null;
			} else if (!status.isOK()) {
				StatusManager.log("search error", this);
				StatusManager.log(status);
				return null;
			}
			return searchCollector;
		} catch (LoginException e) {
			// save this exception to throw later
			this.loginException = e;
		}
		return null;
	}

	/**
	 * Perform a search for qualified instances of the member
	 * 
	 * @param monitor
	 *            The progress monitor to use
	 * @return The QueryHitCollector with the search results
	 */
	private ProgressQueryHitCollector searchQualified(String repositoryUrl, IProgressMonitor monitor) {
		// create a new collector for the results
		collector = new ProgressQueryHitCollector(TasksUiPlugin.getTaskListManager().getTaskList(), new TaskFactory(null));//SearchHitCollector(TasksUiPlugin.getTaskListManager().getTaskList());
		//collector.setOperation(this);
		collector.setProgressMonitor(monitor);

		// get the search url
		String url = Util.getExactSearchURL(repositoryUrl, javaElement);
		TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
				BugzillaCorePlugin.REPOSITORY_KIND, repositoryUrl);
		return search(url, repository, collector, monitor);
	}

	/**
	 * Perform a search for unqualified instances of the member
	 * 
	 * @param monitor
	 *            The progress monitor to use
	 * @return The QueryHitCollector with the search results
	 */
	private ProgressQueryHitCollector searchUnqualified(String repositoryUrl, IProgressMonitor monitor) {
		// create a new collector for the results
		collector = new ProgressQueryHitCollector(TasksUiPlugin.getTaskListManager().getTaskList(), new TaskFactory(null));//SearchHitCollector(TasksUiPlugin.getTaskListManager().getTaskList());
		//collector.setOperation(this);
		collector.setProgressMonitor(monitor);

		// get the search url
		String url = Util.getInexactSearchURL(repositoryUrl, javaElement);
		TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
				BugzillaCorePlugin.REPOSITORY_KIND, repositoryUrl);

		return search(url, repository, collector, monitor);
	}

	/**
	 * Perform a second pass parse to determine if there are any stack traces in
	 * the bug - currently only used for the exact search results
	 * 
	 * @param doiList -
	 *            the list of BugzillaSearchHitDOI elements to parse
	 */
	public static void secondPassBugzillaParser(List<BugzillaReportInfo> doiList) {

		// go through each of the items in the doiList
		for (BugzillaReportInfo info : doiList) {

			// get the bug report so that we have all of the data
			// - descriptions, comments, etc
			RepositoryTaskData b = null;
			try {
				b = info.getBug();
			} catch (Exception e) {
				// don't care since null will be caught
			}

			// if the report could not be downloaded, try the next one
			if (b == null)
				continue;

			// see if the summary has a stack trace in it
			StackTrace[] stackTrace = StackTrace.getStackTrace(b.getDescription(), b.getDescription());
			if (stackTrace != null) {

				// add the stack trace to the doi info
				info.setExact(true);
				info.addStackTraces(stackTrace);
			}

			// go through all of the comments for the bug
			Iterator<TaskComment> comItr = b.getComments().iterator();
			while (comItr.hasNext()) {
				TaskComment taskComment = comItr.next();
				String commentText = taskComment.getText();

				// see if the comment has a stack trace in it
				stackTrace = StackTrace.getStackTrace(commentText, taskComment);
				if (stackTrace != null) {

					// add the stack trace to the doi info
					info.setExact(true);
					info.addStackTraces(stackTrace);
				}
			}
		}
	}

	/**
	 * Add the results returned to the Hash of landmarks
	 * 
	 * @param results
	 *            The list of results
	 * @param isExact
	 *            whether the search was exact or not
	 */
	private List<BugzillaReportInfo> getDoiList(Set<AbstractTask> results) {
		List<BugzillaReportInfo> doiList = new ArrayList<BugzillaReportInfo>();

		boolean isExact = (scope == BugzillaMylarSearch.FULLY_QUAL || scope == BugzillaMylarSearch.LOCAL_QUAL) ? true
				: false;

		BugzillaReportInfo info = null;
		// go through all of the results and create a DoiInfo list
		for (AbstractTask hit : results) {

			try {
				float value = 0;
				info = new BugzillaReportInfo(value, (BugzillaTask)hit, isExact);

				// only download the bug for the exact matches
				// downloading bugs kills the time - can we do this elsewhere? -
				// different thread? persistant?
				// if(isExact){
				// // get the bug report for the doi info item
				// BugReport b = BugzillaRepositoryUtil.getInstance().getBug(
				// hit.getId());
				// // add the bug to the doi info for future use
				// info.setBug(b);
				// }

			} catch (Exception e) {
				StatusManager.log(e, "search failed");
			} finally {
				doiList.add(info);
			}
		}
		return doiList;
	}

	/**
	 * @see org.eclipse.mylyn.internal.bugs.core.search.IBugzillaSearchOperation#getSynchronizationStatus()
	 */
	public IStatus getStatus() throws LoginException {
		// if a LoginException was thrown while trying to search, throw this
		if (loginException == null) {
			return status;
		} else {
			throw loginException;
		}
	}

	/**
	 * @see org.eclipse.mylyn.internal.bugs.core.search.IBugzillaSearchOperation#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/**
	 * Get the member that we are performing the search for
	 * 
	 * @return The member this search is being performed for
	 */
	public IMember getSearchMember() {
		return javaElement;
	}

	/**
	 * Get the name of the member that we are searching for
	 * 
	 * @return The fully qualified name of the member
	 */
	public String getSearchMemberName() {
		return name;
	}

	/**
	 * @see org.eclipse.mylyn.internal.bugs.core.search.IBugzillaSearchOperation#getQuery()
	 */
	public AbstractRepositorySearchQuery getQuery() {
		return query;
	}

	/**
	 * @see org.eclipse.mylyn.internal.bugs.core.search.IBugzillaSearchOperation#setQuery(org.eclipse.mylyn.internal.bugs.core.search.AbstractRepositorySearchQuery)
	 */
	public void setQuery(AbstractRepositorySearchQuery newQuery) {
		this.query = newQuery;
	}

	/**
	 * Get the name of the element that we are searching for
	 * 
	 * @return The name of the element
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the scope of the search operation
	 * 
	 * @return The scope - defined in BugzillaMylarSearch
	 */
	public int getScope() {
		return scope;
	}
}
