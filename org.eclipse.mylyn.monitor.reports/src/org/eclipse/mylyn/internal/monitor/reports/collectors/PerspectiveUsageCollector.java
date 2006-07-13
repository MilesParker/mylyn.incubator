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

package org.eclipse.mylar.internal.monitor.reports.collectors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mylar.context.core.InteractionEvent;
import org.eclipse.mylar.monitor.monitors.PerspectiveChangeMonitor;
import org.eclipse.mylar.monitor.reports.IUsageCollector;

/**
 * @author Mik Kersten and Leah Findlater
 * 
 * TODO: put unclassified events in dummy perspective
 */
public class PerspectiveUsageCollector implements IUsageCollector {

	private Map<String, Integer> perspectiveUsage = new HashMap<String, Integer>();

	private String currentPerspective = "";

	private int numUnassociatedEvents = 0;

	private int numEvents = 0;

	public void consumeEvent(InteractionEvent event, int userId) {
		numEvents++;
		if (event.getKind().equals(InteractionEvent.Kind.PREFERENCE)) {
			if (event.getDelta().equals(PerspectiveChangeMonitor.PERSPECTIVE_ACTIVATED)) {
				currentPerspective = event.getOriginId();
				if (!perspectiveUsage.containsKey(event.getOriginId())) {
					perspectiveUsage.put(event.getOriginId(), 0);
				}
			}
		} else {
			if (perspectiveUsage.containsKey(currentPerspective)) {
				perspectiveUsage.put(currentPerspective, perspectiveUsage.get(currentPerspective) + 1);
			} else {
				numUnassociatedEvents++;
			}
		}
	}

	public List<String> getReport() {
		List<String> summaries = new ArrayList<String>();
		summaries.add("Perspectives (based on total user events, with " + numUnassociatedEvents
				+ " unclassified events)");
		summaries.add(" ");

		List<String> perspectiveUsageList = new ArrayList<String>();
		for (String perspective : perspectiveUsage.keySet()) {
			float perspectiveUse = 100 * perspectiveUsage.get(perspective) / (numEvents);
			String formattedPerspectiveUse = ("" + perspectiveUse);
			int indexOf2ndDecimal = formattedPerspectiveUse.indexOf('.') + 3;
			if (indexOf2ndDecimal <= formattedPerspectiveUse.length()) {
				formattedPerspectiveUse = formattedPerspectiveUse.substring(0, indexOf2ndDecimal);
			}
			String perspectiveName = perspective; // .substring(perspective.lastIndexOf(".")+1,
													// perspective.length());
			if (perspectiveName.contains("Perspective")) {
				perspectiveName = perspectiveName.substring(0, perspectiveName.indexOf("Perspective"));
			}
			perspectiveUsageList.add(formattedPerspectiveUse + "%: " + perspectiveName + " ("
					+ perspectiveUsage.get(perspective) + ")");
		}
		Collections.sort(perspectiveUsageList, new PercentUsageComparator());
		for (String perspectiveUsageSummary : perspectiveUsageList) {
			summaries.add("<br>" + perspectiveUsageSummary);
		}

		if (perspectiveUsage.size() % 2 != 0)
			summaries.add(" ");
		return summaries;
	}

	public String getReportTitle() {
		return "Perspective Usage";
	}

	public void exportAsCSVFile(String directory) {
		String filename = directory + File.separator + "PerspectiveUsage.csv";

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));

			// Write header
			writer.write("Perspective");
			writer.write(",");
			writer.write("Events");
			writer.newLine();

			// Write Data
			for (String perspective : perspectiveUsage.keySet()) {
				writer.write(perspective);
				writer.write(",");
				writer.write(new Integer(perspectiveUsage.get(perspective)).toString());
				writer.newLine();
			}

			writer.write("Unclassified");
			writer.write(",");
			writer.write(numUnassociatedEvents);
			writer.newLine();

			writer.flush();
			writer.close();

		} catch (IOException e) {
			System.err.println("Unable to write CVS file <" + filename + ">");
			e.printStackTrace(System.err);
		}

	}

}