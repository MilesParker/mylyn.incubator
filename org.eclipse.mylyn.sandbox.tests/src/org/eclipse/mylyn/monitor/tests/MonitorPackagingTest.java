/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.mylyn.context.sdk.util.AbstractContextTest;
import org.eclipse.mylyn.internal.commons.core.ZipFileUtil;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;

/**
 * @author Mik Kersten
 */
public class MonitorPackagingTest extends AbstractContextTest {

	public void testCreateUploadPackage() throws IOException, InterruptedException {
		UiUsageMonitorPlugin.getDefault().getInteractionLogger().stopMonitoring();

		File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();
		List<File> files = new ArrayList<File>();
		files.add(monitorFile);

		File zipFile = new File(monitorFile.getParentFile(), "usage-upload.zip");
		zipFile.deleteOnExit();
		ZipFileUtil.createZipFile(zipFile, files);

		UiUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();

		// pretend to upload
		Thread.sleep(1000);

		ZipFile zf = new ZipFile(zipFile);
		try {
			int numEntries = 0;
			for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
				numEntries++;
				String zipEntryName = ((ZipEntry) entries.nextElement()).getName();
				assertTrue("Unknown Entry: " + zipEntryName, zipEntryName.compareTo(monitorFile.getName()) == 0);// ||
			}
			assertEquals("Results not correct size", 1, numEntries);
		} finally {
			zf.close();
		}

		zipFile.delete();
	}

	public void testCreateLargeUploadPackage() throws IOException, InterruptedException {
		for (int i = 0; i < 20000; i++) {
			MonitorUiPlugin.getDefault().notifyInteractionObserved(mockSelection());
		}
		testCreateUploadPackage();
	}

}
