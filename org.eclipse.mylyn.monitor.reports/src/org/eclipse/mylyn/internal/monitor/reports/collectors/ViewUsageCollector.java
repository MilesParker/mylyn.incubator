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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mylar.context.core.InteractionEvent;
import org.eclipse.mylar.monitor.reports.IUsageCollector;

/**
 * @author Mik Kersten
 * @author Leah Findlater
 */
public class ViewUsageCollector implements IUsageCollector {

	protected Map<Integer, Integer> usersNumSelections = new HashMap<Integer, Integer>();

	protected Map<Integer, Map<String, Integer>> usersNormalViewSelections = new HashMap<Integer, Map<String, Integer>>();

	protected int maxViewsToReport = -1;

	public void consumeEvent(InteractionEvent event, int userId) {
		if (!usersNumSelections.containsKey(userId)) {
			usersNumSelections.put(userId, 0);
		}

		Map<String, Integer> normalViewSelections = usersNormalViewSelections.get(userId);
		if (normalViewSelections == null) {
			normalViewSelections = new HashMap<String, Integer>();
			usersNormalViewSelections.put(userId, normalViewSelections);
		}

		if (event.getKind().equals(InteractionEvent.Kind.SELECTION)) {
			if (!usersNumSelections.containsKey(userId))
				usersNumSelections.put(userId, 0);
			int numEvents = usersNumSelections.get(userId) + 1;
			usersNumSelections.put(userId, numEvents);

			String viewId = event.getOriginId();
			if (!normalViewSelections.containsKey(viewId))
				normalViewSelections.put(viewId, 0);
			int normal = normalViewSelections.get(viewId) + 1;
			normalViewSelections.put(viewId, normal);
		}
	}

	public List<String> getSummary(int userId) {
		Map<String, Integer> normalViewSelections = usersNormalViewSelections.get(userId);

		float numSelections = (float) usersNumSelections.get(userId);

		List<String> summaries = new ArrayList<String>();
		List<String> viewUsage = new ArrayList<String>();
		for (String view : normalViewSelections.keySet()) {
			float viewUse = ((float) (normalViewSelections.get(view))) / numSelections;
			String formattedViewUse = formatAsPercentage(viewUse);
			viewUsage.add(formattedViewUse + ": " + view + " (" + normalViewSelections.get(view) + ")" + "<br>");
		}
		Collections.sort(viewUsage, new PercentUsageComparator());
		int numViewsToReport = 0;
		for (String viewUsageSummary : viewUsage) {
			if (numViewsToReport == -1 || numViewsToReport < maxViewsToReport) {
				summaries.add(viewUsageSummary);
				numViewsToReport++;
			}
		}
		return summaries;
	}

	private String formatAsPercentage(float viewUse) {
		String formattedViewUse = ("" + viewUse * 100);
		int indexOf2ndDecimal = formattedViewUse.indexOf('.') + 3;
		if (indexOf2ndDecimal <= formattedViewUse.length()) {
			formattedViewUse = formattedViewUse.substring(0, indexOf2ndDecimal);
		}
		return formattedViewUse + "%";
	}

	public List<String> getReport() {
		List<String> summaries = new ArrayList<String>();
		for (int userId : usersNormalViewSelections.keySet()) {
			summaries.addAll(getSummary(userId));
		}
		return summaries;
	}

	public String getReportTitle() {
		return "View Usage";
	}

	public void exportAsCSVFile(String directory) {
		// TODO Auto-generated method stub

	}

	/**
	 * For testing.
	 */
	public Map<String, Integer> getNormalViewSelections() {
		Map<String, Integer> normalViewSelections = new HashMap<String, Integer>();
		for (int userId : usersNormalViewSelections.keySet()) {
			normalViewSelections.putAll(usersNormalViewSelections.get(userId));
		}
		return normalViewSelections;
	}

	public void setMaxViewsToReport(int maxViewsToReport) {
		this.maxViewsToReport = maxViewsToReport;
	}
}