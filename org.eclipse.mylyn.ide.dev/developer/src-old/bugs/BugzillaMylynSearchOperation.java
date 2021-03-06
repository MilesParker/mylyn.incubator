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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.sandbox.ui.SandboxUiPlugin;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskData;
import org.eclipse.mylyn.internal.tasks.core.deprecated.TaskComment;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.deprecated.TaskFactory;
import org.eclipse.mylyn.internal.tasks.ui.search.AbstractRepositorySearchQuery;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Bugzilla search operation for Mylar
 * 
 * @author Shawn Minto
 * @author Mik Kersten
 */
public class BugzillaMylynSearchOperation extends WorkspaceModifyOperation implements IBugzillaSearchOperation {

	/** The IMember we are doing the search for */
	private final IMember javaElement;

	/** The bugzilla collector for the search */
	private ProgressQueryHitCollector collector = null;//SearchHitCollector

	/** The status of the search operation */
	private IStatus status;

	/** The LoginException that was thrown when trying to do the search */
	private LoginException loginException = null;

	/** The fully qualified name of the member we are searching for */
	private final String name;

	/** The bugzilla search query */
	private AbstractRepositorySearchQuery query;

	private final BugzillaMylynSearch search;

	private final int scope;

	public BugzillaMylynSearchOperation(BugzillaMylynSearch search, IMember m, int scope) {
		this.javaElement = m;
		this.search = search;
		this.scope = scope;
		name = getFullyQualifiedName(m);
	}

	/**
	 * Get the fully qualified name of a IMember TODO: move to a more central location so that others can use this, but
	 * don't want to add unecessary coupling
	 * 
	 * @return String representing the fully qualified name
	 */
	public static String getFullyQualifiedName(IJavaElement je) {
		if (!(je instanceof IMember)) {
			return null;
		}

		IMember m = (IMember) je;
		if (m.getDeclaringType() == null) {
			return ((IType) m).getFullyQualifiedName();
		} else {
			return m.getDeclaringType().getFullyQualifiedName() + "." + m.getElementName();
		}
	}

	@Override
	public void execute(IProgressMonitor monitor) {

		ProgressQueryHitCollector searchCollector = null;

		if (scope == BugzillaMylynSearch.FULLY_QUAL) {
			searchCollector = searchQualified(search.getServerUrl(), monitor);
		} else if (scope == BugzillaMylynSearch.UNQUAL) {
			searchCollector = searchUnqualified(search.getServerUrl(), monitor);
		} else if (scope == BugzillaMylynSearch.LOCAL_QUAL) {
			searchCollector = searchLocalQual(monitor);
		} else if (scope == BugzillaMylynSearch.LOCAL_UNQUAL) {
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
		MylynBugsManager.getBridge().addToLandmarksHash(doiList, javaElement, scope);
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
	@SuppressWarnings("deprecation")
	private ProgressQueryHitCollector searchLocalQual(IProgressMonitor monitor) {

		// get the fully qualified name for searching
		String elementName = getFullyQualifiedName(javaElement);

		// setup the search result collector
		collector = new ProgressQueryHitCollector(TasksUiInternal.getTaskList(), new TaskFactory(null));//SearchHitCollector(TasksUiPlugin.getTaskList());
		//collector.setOperation(this);
		collector.setProgressMonitor(monitor);

		// get all of the root tasks and start the search
		// FIXME
//		Set<AbstractTask> tasks = TasksUiPlugin.getTaskList().getOrphanContainer(
//				LocalRepositoryConnector.REPOSITORY_URL).getChildren();
		Set<ITask> tasks = new HashSet<ITask>();
		searchLocal(tasks, collector, elementName, monitor);
		for (AbstractTaskCategory cat : TasksUiPlugin.getTaskList().getTaskCategories()) {
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
	@SuppressWarnings("deprecation")
	private ProgressQueryHitCollector searchLocalUnQual(IProgressMonitor monitor) {

		// get the element name for searching
		String elementName = javaElement.getElementName();

		// setup the search result collector
		collector = new ProgressQueryHitCollector(TasksUiInternal.getTaskList(), new TaskFactory(null));//SearchHitCollector(TasksUiPlugin.getTaskList());
		//collector.setOperation(this);
		collector.setProgressMonitor(monitor);

		// get all of the root tasks and start the search
		// FIXME
//		Set<AbstractTask> tasks = TasksUiPlugin.getTaskList().getOrphanContainer(
//				LocalRepositoryConnector.REPOSITORY_URL).getChildren();
		Set<ITask> tasks = new HashSet<ITask>();
		searchLocal(tasks, collector, elementName, monitor);
		for (AbstractTaskCategory cat : TasksUiPlugin.getTaskList().getTaskCategories()) {
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
	private void searchLocal(Collection<ITask> tasks, ProgressQueryHitCollector searchCollector, String elementName,
			IProgressMonitor monitor) {
		if (tasks == null) {
			return;
		}

		// go through all of the tasks
		for (ITask task : tasks) {
			monitor.worked(1);

			// check what kind of task it is
//			if (task instanceof BugzillaTask) {

			// we have a bugzilla task, so get the bug report
//				BugzillaTask bugTask = (BugzillaTask) task;
			RepositoryTaskData bugTaskData = TasksUiPlugin.getTaskDataStorageManager().getNewTaskData(
					task.getRepositoryUrl(), task.getTaskId());
			//RepositoryTaskData bugTaskData = bugTask.getTaskData();

			// parse the bug report for the element that we are searching
			// for
			boolean isHit = search(elementName, bugTaskData);

			// determine if we have a hit or not
			if (isHit) {
//					// make a search hit from the bug and then add it to the collector
//					BugzillaQueryHit hit = new BugzillaQueryHit(TasksUiPlugin.getTaskList(), bugTaskData.getDescription(), "", bugTaskData.getRepositoryUrl(), bugTaskData.getId(), null, "");
//					BugzillaTask task = new BugzillaTask();
				// FIXME
				//					searchCollector.accept(bugTask);
			}
//			}
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

		if (bug == null) {
			return false; // MIK: added null check here
		}
		String description = bug.getDescription();
		String summary = bug.getSummary();
		List<TaskComment> taskComments = bug.getComments();

		// search the summary and the summary
		if (Util.hasElementName(elementName, summary)) {
			return true;
		}

		if (Util.hasElementName(elementName, description)) {
			return true;
		}

		Iterator<TaskComment> comItr = taskComments.iterator();
		while (comItr.hasNext()) {
			TaskComment taskComment = comItr.next();
			String commentText = taskComment.getText();
			// search the text for a reference to the element
			if (Util.hasElementName(elementName, commentText)) {
				return true;
			}
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
				MultiStatus errorStatus = new MultiStatus(SandboxUiPlugin.ID_PLUGIN, 0, "Search error", null);
				errorStatus.add(status);
				StatusHandler.fail(errorStatus);
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
	@SuppressWarnings("deprecation")
	private ProgressQueryHitCollector searchQualified(String repositoryUrl, IProgressMonitor monitor) {
		// create a new collector for the results
		collector = new ProgressQueryHitCollector(TasksUiInternal.getTaskList(), new TaskFactory(null));//SearchHitCollector(TasksUiPlugin.getTaskList());
		//collector.setOperation(this);
		collector.setProgressMonitor(monitor);

		// get the search url
		String url = Util.getExactSearchURL(repositoryUrl, javaElement);
		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(BugzillaCorePlugin.CONNECTOR_KIND,
				repositoryUrl);
		return search(url, repository, collector, monitor);
	}

	/**
	 * Perform a search for unqualified instances of the member
	 * 
	 * @param monitor
	 *            The progress monitor to use
	 * @return The QueryHitCollector with the search results
	 */
	@SuppressWarnings("deprecation")
	private ProgressQueryHitCollector searchUnqualified(String repositoryUrl, IProgressMonitor monitor) {
		// create a new collector for the results
		collector = new ProgressQueryHitCollector(TasksUiInternal.getTaskList(), new TaskFactory(null));//SearchHitCollector(TasksUiPlugin.getTaskList());
		//collector.setOperation(this);
		collector.setProgressMonitor(monitor);

		// get the search url
		String url = Util.getInexactSearchURL(repositoryUrl, javaElement);
		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(BugzillaCorePlugin.CONNECTOR_KIND,
				repositoryUrl);

		return search(url, repository, collector, monitor);
	}

	/**
	 * Perform a second pass parse to determine if there are any stack traces in the bug - currently only used for the
	 * exact search results
	 * 
	 * @param doiList
	 *            - the list of BugzillaSearchHitDOI elements to parse
	 */
	public static void secondPassBugzillaParser(List<BugzillaReportInfo> doiList) {

		// go through each of the items in the doiList
		for (BugzillaReportInfo info : doiList) {

			// get the bug report so that we have all of the data
			// - descriptions, comments, etc
			TaskData b = null;
			try {
				b = info.getBug();
			} catch (Exception e) {
				// don't care since null will be caught
			}

			// if the report could not be downloaded, try the next one
			if (b == null) {
				continue;
			}

			// Add back:
			// see if the summary has a stack trace in it
//			StackTrace[] stackTrace = StackTrace.getStackTrace(b.getDescription(), b.getDescription());
//			if (stackTrace != null) {
//
//				// add the stack trace to the doi info
//				info.setExact(true);
//				info.addStackTraces(stackTrace);
//			}

			// Add back:
			// go through all of the comments for the bug
//			Iterator<TaskComment> comItr = b.getComments().iterator();
//			while (comItr.hasNext()) {
//				TaskComment taskComment = comItr.next();
//				String commentText = taskComment.getText();
//
//				// see if the comment has a stack trace in it
//				stackTrace = StackTrace.getStackTrace(commentText, taskComment);
//				if (stackTrace != null) {
//
//					// add the stack trace to the doi info
//					info.setExact(true);
//					info.addStackTraces(stackTrace);
//				}
//			}
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

		boolean isExact = (scope == BugzillaMylynSearch.FULLY_QUAL || scope == BugzillaMylynSearch.LOCAL_QUAL) ? true
				: false;

		BugzillaReportInfo info = null;
		// go through all of the results and create a DoiInfo list
		for (ITask hit : results) {

			try {
				float value = 0;
				info = new BugzillaReportInfo(value, hit, isExact);

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
				StatusHandler.log(new Status(IStatus.INFO, SandboxUiPlugin.ID_PLUGIN, "Search failed", e));
			} finally {
				doiList.add(info);
			}
		}
		return doiList;
	}

	/**
	 * @see org.eclipse.mylyn.internal.bugs.core.search.IBugzillaSearchOperation#getStatus()
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
