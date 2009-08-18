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

package org.eclipse.mylyn.internal.monitor.usage.editors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.monitor.usage.ReportGenerator;
import org.eclipse.mylyn.internal.monitor.usage.StudyParameters;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Mik Kersten
 */
public class UsageStatsEditorInput implements IEditorInput {

	private final ReportGenerator reportGenerator;

	private final List<File> usageFiles;

	/**
	 * Supports either the single workspace file or multiple zip files.
	 */
	public UsageStatsEditorInput(List<File> files, ReportGenerator reportGenerator, StudyParameters studyParameters) {
		// try {
		this.reportGenerator = reportGenerator;
		usageFiles = files;
		this.studyParameters = studyParameters;
		reportGenerator.getStatisticsFromInteractionHistories(usageFiles, (IJobChangeListener) null);
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return Messages.UsageStatsEditorInput_Usage_Summary;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return NLS.bind(Messages.UsageStatsEditorInput_X_Usage_Statistics, studyParameters.getStudyName());
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public List<File> getInputFiles() {
		return usageFiles;
	}

	public ReportGenerator getReportGenerator() {
		return reportGenerator;
	}

	private final byte[] buffer = new byte[8192];

	private final StudyParameters studyParameters;

	public void transferData(InputStream sourceStream, OutputStream destination) throws IOException {
		int bytesRead = 0;
		while (bytesRead != -1) {
			bytesRead = sourceStream.read(buffer, 0, buffer.length);
			if (bytesRead != -1) {
				destination.write(buffer, 0, bytesRead);
			}
		}
	}

	public StudyParameters getStudyParameters() {
		return studyParameters;
	}
}
