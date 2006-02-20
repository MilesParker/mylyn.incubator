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

package org.eclipse.mylar.monitor.reports;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylar.provisional.core.InteractionEvent;

/**
 * @author Mik Kersten
 */
public class DelegatingUsageCollector implements IUsageCollector {

	protected List<IUsageScanner> scanners = new ArrayList<IUsageScanner>();

	public void addScanner(IUsageScanner aScanner) {
		scanners.add(aScanner);
	}

	private List<IUsageCollector> delegates = new ArrayList<IUsageCollector>();

	private String reportTitle = "";

	public List<IUsageCollector> getDelegates() {
		return delegates;
	}

	public void setDelegates(List<IUsageCollector> delegates) {
		this.delegates = delegates;
	}

	public void consumeEvent(InteractionEvent event, int userId) {
		for (IUsageCollector collector : delegates) {
			collector.consumeEvent(event, userId);
		}
	}

	public List<String> getReport() {
		List<String> combinedReports = new ArrayList<String>();
		for (IUsageCollector collector : delegates) {
			combinedReports.add("<h3>" + collector.getReportTitle() + "</h3>");
			combinedReports.addAll(collector.getReport());
		}
		return combinedReports;
	}

	public void exportAsCSVFile(String directory) {

	}

	public String getReportTitle() {
		return reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

}
