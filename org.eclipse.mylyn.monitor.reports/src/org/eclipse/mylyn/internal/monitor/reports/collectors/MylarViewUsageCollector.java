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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.mylar.context.core.InteractionEvent;
import org.eclipse.mylar.internal.context.ui.actions.AbstractApplyMylarAction;
import org.eclipse.mylar.internal.tasks.ui.actions.TaskActivateAction;
import org.eclipse.mylar.internal.tasks.ui.actions.TaskDeactivateAction;

/**
 * @author Mik Kersten
 */
public class MylarViewUsageCollector extends ViewUsageCollector {

	private Set<Integer> mylarUserIds = new HashSet<Integer>();

	Map<Integer, Map<String, Integer>> usersFilteredViewSelections = new HashMap<Integer, Map<String, Integer>>();

	private Map<Integer, Set<String>> usersFilteredViews = new HashMap<Integer, Set<String>>();

	Map<Integer, Integer> usersNumDecayed = new HashMap<Integer, Integer>();

	Map<Integer, Integer> usersNumDefault = new HashMap<Integer, Integer>();

	Map<Integer, Integer> usersNumNew = new HashMap<Integer, Integer>();

	Map<Integer, Integer> usersNumPredicted = new HashMap<Integer, Integer>();

	Map<Integer, Integer> usersNumUnknown = new HashMap<Integer, Integer>();

	@Override
	public void consumeEvent(InteractionEvent event, int userId) {
		super.consumeEvent(event, userId);

		if (event.getKind().equals(InteractionEvent.Kind.COMMAND)) {
			if (event.getOriginId().equals(TaskActivateAction.ID)) {
				mylarUserIds.add(userId);
			} else if (event.getOriginId().equals(TaskDeactivateAction.ID)) {
				mylarUserIds.remove(userId);
			}
		}

		Set<String> filteredViews = usersFilteredViews.get(userId);
		if (filteredViews == null) {
			filteredViews = new HashSet<String>();
			usersFilteredViews.put(userId, filteredViews);
		}

		Map<String, Integer> filteredViewSelections = usersFilteredViewSelections.get(userId);
		if (filteredViewSelections == null) {
			filteredViewSelections = new HashMap<String, Integer>();
			usersFilteredViewSelections.put(userId, filteredViewSelections);
		}

		if (event.getKind().equals(InteractionEvent.Kind.SELECTION)) {
			String viewId = event.getOriginId();
			
			// TODO: put back?
//			if (mylarUserIds.contains(userId)) {
//				if (event.getDelta().equals(SelectionMonitor.SELECTION_DECAYED)) {
//					if (!usersNumDecayed.containsKey(userId))
//						usersNumDecayed.put(userId, 0);
//					int numDecayed = usersNumDecayed.get(userId) + 1;
//					usersNumDecayed.put(userId, numDecayed);
//				} else if (event.getDelta().equals(SelectionMonitor.SELECTION_PREDICTED)) {
//					if (!usersNumPredicted.containsKey(userId))
//						usersNumPredicted.put(userId, 0);
//					int numPredicted = usersNumPredicted.get(userId) + 1;
//					usersNumPredicted.put(userId, numPredicted);
//				} else if (event.getDelta().equals(SelectionMonitor.SELECTION_NEW)) {
//					if (!usersNumNew.containsKey(userId))
//						usersNumNew.put(userId, 0);
//					int numNew = usersNumNew.get(userId) + 1;
//					usersNumNew.put(userId, numNew);
//				} else if (event.getDelta().equals(SelectionMonitor.SELECTION_DEFAULT)) {
//					if (!usersNumDefault.containsKey(userId))
//						usersNumDefault.put(userId, 0);
//					int numDefault = usersNumDefault.get(userId) + 1;
//					usersNumDefault.put(userId, numDefault);
//				} else {
//					if (!usersNumUnknown.containsKey(userId))
//						usersNumUnknown.put(userId, 0);
//					int numUnknownNew = usersNumUnknown.get(userId) + 1;
//					usersNumUnknown.put(userId, numUnknownNew);
//				}
//			}

			if (filteredViews.contains(viewId)) {
				if (!filteredViewSelections.containsKey(viewId))
					filteredViewSelections.put(viewId, 0);
				int filtered = filteredViewSelections.get(viewId) + 1;
				filteredViewSelections.put(viewId, filtered);
			}

		} else if (event.getKind().equals(InteractionEvent.Kind.PREFERENCE)) {
			if (event.getOriginId().startsWith(AbstractApplyMylarAction.PREF_ID_PREFIX)) {
				String viewId = event.getOriginId().substring(AbstractApplyMylarAction.PREF_ID_PREFIX.length());
				if (event.getDelta().equals("true")) {
					filteredViews.add(viewId);
				} else {
					filteredViews.remove(viewId);
				}
			}
		}
	}

	public int getFilteredSelections(int userId, String viewId) {
		Map<String, Integer> filteredViewSelections = usersFilteredViewSelections.get(userId);
		if (filteredViewSelections.containsKey(viewId)) {
			return filteredViewSelections.get(viewId);
		} else {
			return 0;
		}
	}

	@Override
	public List<String> getSummary(int userId) {

		List<String> summaries = new ArrayList<String>();
		Map<String, Integer> filteredViewSelections = usersFilteredViewSelections.get(userId);
		Map<String, Integer> normalViewSelections = usersNormalViewSelections.get(userId);

		if (!filteredViewSelections.keySet().isEmpty()) {
			summaries.add("<h4>Interest Filtering</h4>");
		}

		for (String view : filteredViewSelections.keySet()) {
			int normalSelections = normalViewSelections.get(view);
			int filteredSelections = filteredViewSelections.get(view);
			int unfilteredSelections = normalSelections - filteredSelections;
			summaries.add(view + " filtered: " + filteredSelections + " vs. unfiltered: ");
			summaries.add(unfilteredSelections + "<br>");
		}
		summaries.add("<h4>View Usage ");
		List<String> allSummaries = super.getSummary(userId);
		if (maxViewsToReport != -1 && allSummaries.size() == maxViewsToReport) {
			summaries.add("(top "+ maxViewsToReport + ")");
		}
		summaries.add("</h4>");
		summaries.addAll(allSummaries);

//		summaries.add("<h4>Interest Model</h4>");
//		int numNew = 0;
//		if (usersNumNew.containsKey(userId))
//			numNew = usersNumNew.get(userId);
//		int numPredicted = 0;
//		if (usersNumPredicted.containsKey(userId))
//			numPredicted = usersNumPredicted.get(userId);
//		int numInteresting = 0;
//		if (usersNumDefault.containsKey(userId))
//			numInteresting = usersNumDefault.get(userId);
//		int numDecayed = 0;
//		if (usersNumDecayed.containsKey(userId))
//			numDecayed = usersNumDecayed.get(userId);
//		int numUnknown = 0;
//		if (usersNumUnknown.containsKey(userId))
//			numUnknown = usersNumUnknown.get(userId);
//
//		float numSelections = numNew + numPredicted + numInteresting + numDecayed + numUnknown;
//		float inModel = (numPredicted + numInteresting + numDecayed);
//		float notInModel = numNew;
//		float hitRatio = inModel / (inModel + notInModel);
//		summaries.add("In model (inModel / (inModel + notInModel): " + ReportGenerator.formatPercentage(hitRatio) + "<br>");
// 
//		summaries.add("New: " + ReportGenerator.formatPercentage(numNew / numSelections) + "(" + numNew + ")" + "; ");
//		summaries.add("Predicted: " + ReportGenerator.formatPercentage(numPredicted / numSelections) + " (" + numPredicted + ")"
//				+ "; ");
//		summaries.add("Interesting: " + ReportGenerator.formatPercentage(numInteresting / numSelections) + " (" + numInteresting
//				+ ")" + "; ");
//		summaries.add("Decayed: " + ReportGenerator.formatPercentage(numDecayed / numSelections) + " (" + numDecayed + ")" + "; ");
//		summaries.add("Unknown: " + ReportGenerator.formatPercentage(numUnknown / numSelections) + " (" + numUnknown + ")" + "<br>");
	
		return summaries;
	}

	@Override
	public List<String> getReport() {
		List<String> summaries = new ArrayList<String>();
		for (int userId : usersNormalViewSelections.keySet()) {
			summaries.addAll(getSummary(userId));
		}
		return summaries;
	}

	@Override
	public String getReportTitle() {
		return "Mylar View Usage";
	}

	/**
	 * For testing.
	 */
	public Map<String, Integer> getFilteredViewSelections() {
		Map<String, Integer> filteredViewSelections = new HashMap<String, Integer>();
		for (int userId : usersFilteredViewSelections.keySet()) {
			filteredViewSelections.putAll(usersFilteredViewSelections.get(userId));
		}
		return filteredViewSelections;
	}
}
