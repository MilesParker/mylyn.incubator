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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.eclipse.mylyn.internal.bugzilla.ui.BugzillaUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Utilities methods for the BugzillaMylarBridge
 * 
 * @author Shawn Minto
 * @author Mik Kersten
 */
public class Util {

	/**
	 * List of all of the search repositoryOperations that can be done <br>
	 * all words, any words, regex
	 */
	private static final String[] patternOperationValues = { "allwordssubstr", "anywordssubstr", "regexp" };

	/**
	 * Sugzilla preferences so that we can get the search params
	 */
	// private static IPreferenceStore prefs =
	// BugzillaPlugin.getDefault().getPreferenceStore();
	// private static String[] resolutionValues =
	// BugzillaRepositoryUtil.convertQueryOptionsToArray(prefs.getString(IBugzillaConstants.VALUES_RESOLUTION));
	//   
	// private static String[] statusValues =
	// BugzillaRepositoryUtil.convertQueryOptionsToArray(prefs.getString(IBugzillaConstants.VALUES_STATUS));
	/**
	 * Get the bugzilla url used for searching for exact matches
	 * 
	 * @param je
	 *            The IMember to create the query string for
	 * @return A url string for the search
	 */
	public static String getExactSearchURL(String repositoryUrl, IMember je) {
		StringBuffer sb = getQueryURLStart(repositoryUrl);

		String long_desc = "";

		// get the fully qualified name of the element
		long_desc += BugzillaMylynSearchOperation.getFullyQualifiedName(je);

		try {
			// encode the string to be used as a url
			sb.append(URLEncoder.encode(long_desc, Charset.defaultCharset().toString()));
		} catch (UnsupportedEncodingException e) {
			// should never get here since we are using the default encoding
		}
		sb.append(getQueryURLEnd(repositoryUrl));

		return sb.toString();
	}

	/**
	 * Get the bugzilla url used for searching for inexact matches
	 * 
	 * @param je
	 *            The IMember to create the query string for
	 * @return A url string for the search
	 */
	public static String getInexactSearchURL(String repositoryUrl, IMember je) {
		StringBuffer sb = getQueryURLStart(repositoryUrl);

		String long_desc = "";

		// add the member, qualified with just its parents name
		if (!(je instanceof IType)) {
			long_desc += je.getParent().getElementName() + ".";
		}
		long_desc += je.getElementName();

		try {
			// encode the string to be used as a url
			sb.append(URLEncoder.encode(long_desc, Charset.defaultCharset().toString()));
		} catch (UnsupportedEncodingException e) {
			// should never get here since we are using the default encoding
		}
		sb.append(getQueryURLEnd(repositoryUrl));

		return sb.toString();
	}

	/**
	 * Create the end of the bugzilla query URL with all of the status' and resolutions that we want
	 * 
	 * @return StringBuffer with the end of the query URL in it
	 */
	public static StringBuffer getQueryURLEnd(String repositoryUrl) {

		StringBuffer sb = new StringBuffer();
		TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(repositoryUrl);
		RepositoryConfiguration repositoryConfiguration = null;
		try {
			repositoryConfiguration = BugzillaCorePlugin.getRepositoryConfiguration(repository, false,
					new NullProgressMonitor());
		} catch (final CoreException e) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Bugzilla Search Page",
							"Unable to get configuration. Ensure proper repository configuration in "
									+ TasksUiPlugin.LABEL_VIEW_REPOSITORIES + ".\n\n");
				}
			});
		}

		String[] resolutionValues = BugzillaUiPlugin.getQueryOptions(IBugzillaConstants.VALUES_RESOLUTION, null,
				repositoryConfiguration);

		String[] statusValues = BugzillaUiPlugin.getQueryOptions(IBugzillaConstants.VALUES_STATUS, null,
				repositoryConfiguration);

		// add the status and resolutions that we care about
		sb.append("&bug_status=" + statusValues[0]); // UNCONFIRMED
		sb.append("&bug_status=" + statusValues[1]); // NEW
		sb.append("&bug_status=" + statusValues[2]); // ASSIGNED
		sb.append("&bug_status=" + statusValues[3]); // REOPENED
		sb.append("&bug_status=" + statusValues[4]); // RESOLVED
		sb.append("&bug_status=" + statusValues[5]); // VERIFIED
		sb.append("&bug_status=" + statusValues[6]); // CLOSED

		sb.append("&resolution=" + resolutionValues[0]); // FIXED
		sb.append("&resolution=" + resolutionValues[3]); // LATER
		sb.append("&resolution=" + "---"); // ---
		return sb;
	}

	/**
	 * Create the bugzilla query URL start.
	 * 
	 * @return The start of the query url as a StringBuffer <br>
	 *         Example: https://bugs.eclipse.org/bugs/buglist.cgi?long_desc_type=allwordssubstr&long_desc=
	 */
	public static StringBuffer getQueryURLStart(String repositoryUrl) {
		StringBuffer sb = new StringBuffer(repositoryUrl);

		if (sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}
		sb.append("buglist.cgi?");

		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(BugzillaCorePlugin.CONNECTOR_KIND,
				repositoryUrl);
		if (repository != null && repository.hasCredentials()) {
			// if (BugzillaPreferencePage.getUserName() != null
			// && !BugzillaPreferencePage.getUserName().equals("")
			// && BugzillaPreferencePage.getPassword() != null
			// && !BugzillaPreferencePage.getPassword().equals("")) {
			try {
				sb.append("GoAheadAndLogIn=1&Bugzilla_login=" + URLEncoder.encode(repository.getUserName(), // BugzillaPreferencePage.getUserName(),
						Charset.defaultCharset().toString()) + "&Bugzilla_password="
						+ URLEncoder.encode(repository.getPassword(), // BugzillaPreferencePage.getPassword(),
								Charset.defaultCharset().toString()) + "&");
			} catch (UnsupportedEncodingException e) {
				// should never get here since we are using the default encoding
			}
		}
		// add the summary search type
		sb.append("long_desc_type=");
		sb.append(patternOperationValues[0]); // search for all words
		sb.append("&long_desc=");

		return sb;
	}

	/**
	 * Search the given string for another string
	 * 
	 * @param elementName
	 *            The name of the element that we are looking for
	 * @param comment
	 *            The text to search for this element name
	 * @return <code>true</code> if the element is found in the text else <code>false</code>
	 */
	public static boolean hasElementName(String elementName, String comment) {

		// setup a regex for the element name
		String regexElement = ".*" + elementName + ".*";

		// get all of the individual lines for the string
		String[] lines = comment.split("\n");

		// go through each of the lines of the string
		for (String line : lines) {

			if (line.matches(regexElement)) {
				return true;
			}
		}
		return false;
	}
}
