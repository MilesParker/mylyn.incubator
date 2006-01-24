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

package org.eclipse.mylar.internal.monitor.reports;

import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 */
public class MylarReportsPlugin extends AbstractUIPlugin {

	public static final String REPORT_SUMMARY_ID = "org.eclipse.mylar.monitor.reports.ui.actions.monitorSummaryReport";

	public static final String REPORT_USERS_ID = "org.eclipse.mylar.monitor.reports.ui.actions.monitorUsersReport";

	public static final String SHARED_TASK_DATA_ROOT_DIR = "org.eclipse.mylar.monitor.reports.preferences.sharedTaskDataRootDir";

	private static MylarReportsPlugin plugin;

	public MylarReportsPlugin() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	public static MylarReportsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylar.internal.monitor.reports", path);
	}

	/**
	 * Returns the root directory of the shared location where task data files
	 * are stored. Returns "" if the preference has not been set.
	 */
	public String getRootSharedDataDirectory() {
		return getPreferenceStore().getString(SHARED_TASK_DATA_ROOT_DIR);
	}
}
