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

package org.eclipse.mylar.internal.monitor.usage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.mylar.context.core.MylarStatusHandler;

/**
 * @author Mik Kersten
 */
public class AbstractMonitorLog {

	protected File outputFile;

	protected FileOutputStream outputStream;

	boolean started = false;

	public AbstractMonitorLog() {
		super();
	}

	public void startMonitoring() {
		synchronized (this) {
			if (started) {
				return;
			} else {
				started = true;
			}
		}
		try {
			if (!outputFile.exists())
				outputFile.createNewFile();
			outputStream = new FileOutputStream(outputFile, true);
		} catch (Exception e) {
			MylarStatusHandler.fail(e, "could not resolve log to file: " + outputFile.getAbsolutePath(), true);
		} 
	}

	public void stopMonitoring() {
		try {
			if (outputStream != null) {
				outputStream.flush();
				outputStream.close();
			}
			started = false;
		} catch (IOException e) {
			MylarStatusHandler.fail(e, "could not close interaction event stream", false);
		}
	}

	public File moveOutputFile(String newPath) {
		stopMonitoring();
		File newFile = new File(newPath);
		try {
			if (outputFile.exists() && !newFile.exists()) {
				outputFile.renameTo(newFile);
			} else if (!newFile.exists()) {
				newFile.createNewFile();
				outputFile.delete();
			} else {
				outputFile.delete();
			}
			this.outputFile = newFile;
		} catch (Exception e) {
			MylarStatusHandler.fail(e, "Could not set logger output file", true);
		}
		startMonitoring();
		return newFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

}