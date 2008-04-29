/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;

/**
 * @author Wesley Coelho
 * @author Mik Kersten
 */
public class SharedDataDirectoryManager {

	/**
	 * True if the shared data directory has been temporarily set for reporting purposes
	 */
	private boolean sharedDataDirectoryInUse = false;

	private String sharedDataDirectory = null;

	/**
	 * Sets the path of a shared data directory to be temporarily used (for reporting). Call useMainDataDirectory() to
	 * return to using the main data directory.
	 */
	public void setSharedDataDirectory(String dirPath) {
		sharedDataDirectory = dirPath;
	}

	/**
	 * Returns the shared data directory path if one has been set. If not, the empty string is returned. Note that the
	 * directory may not currently be in use.
	 */
	public String getSharedDataDirectory() {
		if (sharedDataDirectory != null) {
			return sharedDataDirectory;
		} else {
			return "";
		}
	}

	/**
	 * Set to true to use the shared data directory set with setSharedDataDirectory(String) Set to false to return to
	 * using the main data directory
	 */
	public void setSharedDataDirectoryEnabled(boolean enable) {
		if (enable && sharedDataDirectory == null) {
			StatusHandler.fail(new Status(IStatus.ERROR, SandboxUiPlugin.ID_PLUGIN,
					"Could not enable shared data directory because no shared data directory was specifed.",
					new Exception("EnableDataDirectoryException")));
			return;
		}
		sharedDataDirectoryInUse = enable;
	}

	/**
	 * True if a shared data directory rather than the main data directory is currently in use
	 */
	public boolean isSharedDataDirectoryEnabled() {
		return sharedDataDirectoryInUse;
	}
}
