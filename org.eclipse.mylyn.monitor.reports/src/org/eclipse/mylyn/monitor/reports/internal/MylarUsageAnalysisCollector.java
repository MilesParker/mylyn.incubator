/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.monitor.reports.internal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.mylar.core.InteractionEvent;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.core.util.DateUtil;
import org.eclipse.mylar.monitor.reports.ReportGenerator;
import org.eclipse.mylar.tasklist.ui.actions.TaskActivateAction;
import org.eclipse.mylar.tasklist.ui.actions.TaskDeactivateAction;

/**
 * Delagates to other collectors for additional info.
 * 
 * @author Mik Kersten
 */
public class MylarUsageAnalysisCollector extends AbstractMylarUsageCollector {

	public static final int BASELINE_EDITS_THRESHOLD = 400;
	private static final int MYLAR_EDITS_THRESHOLD = 1200;
	private static final int NUM_VIEWS_REPORTED = 5;
//	private static final int THRESHOLD_SELECTION_RUNS = 0;
//	public static final int JAVA_EDITS_THRESHOLD = 3000;
//	private static final int TASK_DEACTIVATIONS_THRESHOLD = 0;
	
	
	float summaryEditRatioDelta = 0;
	float mylarInactiveDelta = 0;

	final List<Integer> usersImproved = new ArrayList<Integer>();
	final List<Integer> usersDegraded = new ArrayList<Integer>();
		
	private Map<Integer, Date> startDates = new HashMap<Integer, Date>();
	private Map<Integer, Integer> numMylarActiveJavaEdits = new HashMap<Integer, Integer>();
	private Map<Integer, Date> endDates = new HashMap<Integer, Date>();
	
	private Map<Integer, Integer> baselineSelections = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> baselineEdits = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mylarInactiveSelections = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mylarInactiveEdits = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mylarSelections = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mylarEdits = new HashMap<Integer, Integer>();

	private Map<Integer, Integer> baselineCurrentNumSelectionsBeforeEdit = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> baselineTotalSelectionsBeforeEdit = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> baselineTotalEditsCounted = new HashMap<Integer, Integer>();
		
	private Map<Integer, Integer> mylarCurrentNumSelectionsBeforeEdit = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mylarTotalSelectionsBeforeEdit = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mylarTotalEditsCounted = new HashMap<Integer, Integer>();
	
	private Map<Integer, InteractionEvent> lastUserEvent = new HashMap<Integer, InteractionEvent>();
	private Map<Integer, Long> timeMylarActive = new HashMap<Integer, Long>();
	private Map<Integer, Long> timeMylarInactive = new HashMap<Integer, Long>();
	private Map<Integer, Long> timeBaseline = new HashMap<Integer, Long>();
			
	private MylarViewUsageCollector viewUsageCollector = new MylarViewUsageCollector();
	
	public MylarUsageAnalysisCollector() {
		viewUsageCollector.setMaxViewsToReport(NUM_VIEWS_REPORTED);
		super.getDelegates().add(viewUsageCollector);
	}
	
	public String getReportTitle() {
		return "Mylar Usage";
	}
	
	public void consumeEvent(InteractionEvent event, int userId) {
		super.consumeEvent(event, userId);
		if (!startDates.containsKey(userId)) startDates.put(userId, event.getDate());
		endDates.put(userId, event.getDate());
		
		// Mylar is active
		if (mylarUserIds.contains(userId) && !mylarInactiveUserIds.contains(userId)) {
			accumulateDuration(event, userId, timeMylarActive);
			if (isJavaEdit(event)) incrementCount(userId, numMylarActiveJavaEdits);
			if (acceptSelection(event)) {
	    		incrementCount(userId, mylarSelections);
	    		incrementCount(userId, mylarCurrentNumSelectionsBeforeEdit);  		
	        } else if (acceptEdit(event)) {
	        	incrementCount(userId, mylarEdits);
	        	
	        	if (mylarCurrentNumSelectionsBeforeEdit.containsKey((userId))) {
	        		int num = mylarCurrentNumSelectionsBeforeEdit.get(userId);
	        		if (num > 0) {
	        			incrementCount(userId, mylarTotalEditsCounted);
		        		incrementCount(userId, mylarTotalSelectionsBeforeEdit, num);
		        		mylarCurrentNumSelectionsBeforeEdit.put(userId, 0);
	        		}
	        	}
	        }
		// Mylar is inactive
		} else if (mylarInactiveUserIds.contains(userId)) {
			accumulateDuration(event, userId, timeMylarInactive);
			if (acceptSelection(event)) {
	    		incrementCount(userId, mylarInactiveSelections);
			} else if (acceptEdit(event)) {
	        	incrementCount(userId, mylarInactiveEdits);
			}
		// Baseline
		} else { 
			accumulateDuration(event, userId, timeBaseline);
	        if (acceptSelection(event)) {
	    		incrementCount(userId, baselineSelections);
	    		
	    		incrementCount(userId, baselineCurrentNumSelectionsBeforeEdit);
	        } else if (acceptEdit(event)) {
	        	incrementCount(userId, baselineEdits);
	        	
	        	if (baselineCurrentNumSelectionsBeforeEdit.containsKey((userId))) {
	        		int num = baselineCurrentNumSelectionsBeforeEdit.get(userId);
	        		if (num > 0) {
	        			incrementCount(userId, baselineTotalEditsCounted);
	        			incrementCount(userId, baselineTotalSelectionsBeforeEdit, num);
		        		baselineCurrentNumSelectionsBeforeEdit.put(userId, 0);
	        		}
	        	}
	        } 
		}
	}

	private void accumulateDuration(InteractionEvent event, int userId, Map<Integer, Long> timeAccumulator) {
		  // Restart accumulation if greater than 5 min has elapsed between events
		if (lastUserEvent.containsKey(userId)) {
			long elapsed = 
				event.getDate().getTime()
				- lastUserEvent.get(userId).getDate().getTime();
			
			if (elapsed < 5 * 60 * 1000) {
				if (!timeAccumulator.containsKey(userId)) {
					timeAccumulator.put(userId, new Long(0));
				}
				timeAccumulator.put(userId, timeAccumulator.get(userId) + elapsed);
			}
		} 
		lastUserEvent.put(userId, event);
	}

	private boolean acceptEdit(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.EDIT);
	}
	
	private boolean isJavaEdit(InteractionEvent event) {
		return
			event.getKind().equals(InteractionEvent.Kind.EDIT) &&
			(event.getOriginId().contains("java") || event.getOriginId().contains("jdt.ui"));
	}

	private boolean acceptSelection(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.SELECTION)
			&& !event.getOriginId().contains("Editor")
			&& !event.getOriginId().contains("editor")
			&& !event.getOriginId().contains("source");
	}

	private void incrementCount(int userId, Map<Integer, Integer> map, int count) {
		if (!map.containsKey(userId)) map.put(userId, 0);
		map.put(userId, map.get(userId) + count);
	}
	
	private void incrementCount(int userId, Map<Integer, Integer> map) {
		incrementCount(userId, map, 1);
	}
	
	public List<String> getReport() {
		usersImproved.clear();
		usersDegraded.clear();
		int acceptedUsers = 0;
		int rejectedUsers = 0;
		summaryEditRatioDelta = 0;
		mylarInactiveDelta = 0;
		List<String> report = new ArrayList<String>();
		for (Iterator it = userIds.iterator(); it.hasNext(); ) {
    		int id = (Integer)it.next();
    		if (acceptUser(id)) {
				report.add("<h3>USER ID: " + id + " (from: " + getStartDate(id) + " to " + getEndDate(id) + ")</h3>");
				acceptedUsers++;
				
				float baselineRatio = getBaselineRatio(id);
				float mylarInactiveRatio = getMylarInactiveRatio(id);
				float mylarActiveRatio = getMylarRatio(id);		
				float combinedMylarRatio = mylarInactiveRatio + mylarActiveRatio;
				
				float ratioPercentage = (combinedMylarRatio-baselineRatio) / baselineRatio;
				if (ratioPercentage > 0) {
					usersImproved.add(id);
				} else {
					usersDegraded.add(id);
				}
				summaryEditRatioDelta += ratioPercentage;
				String baselineVsMylarRatio = "Baseline vs. Mylar edit ratio: " + baselineRatio + ", mylar: " + combinedMylarRatio + ",  ";
				String ratioChange = formatPercentage(100*ratioPercentage);
				baselineVsMylarRatio += " <b>change: " + ratioChange + "%</b>";
				report.add(baselineVsMylarRatio + "<br>");
				
//				float inactivePercentage = (mylarActiveRatio-mylarInactiveRatio) / mylarInactiveRatio;
//				String inactiveRatioChange = formatPercentage(100*(inactivePercentage));
//				mylarInactiveDelta += inactivePercentage;
//				String inactiveVsActiveRatio = "";
//				inactiveVsActiveRatio += "Inactive vs. Active edit ratio: " + mylarInactiveRatio + ", mylar: " + mylarActiveRatio + ",  ";
//				inactiveVsActiveRatio += " <b>change: " + inactiveRatioChange + "%</b>";
//				report.add(inactiveVsActiveRatio + "<br>");
								
//				if (baselineTotalSelectionsBeforeEdit.containsKey(id) && mylarTotalSelectionsBeforeEdit.containsKey(id)) {	
//					float baselineRuns = 
//						(float)baselineTotalSelectionsBeforeEdit.get(id) /
//						(float)baselineTotalEditsCounted.get(id);
//					
//					float mylarRuns = 
//						(float)mylarTotalSelectionsBeforeEdit.get(id) /
//						(float)mylarTotalEditsCounted.get(id);  
//					
//					float runsPercentage = (mylarRuns-baselineRuns) / baselineRuns;
//					String runsChange = formatPercentage(-100*runsPercentage);
//					report.add("Avg baseline selections before edit: " + baselineRuns 
//							+ " vs. mylar: " + mylarRuns
//							+ " <b>change: " + runsChange + "</b><br>");;
//				}
				
				report.add("<h4>Activity</h4>");
				float editsActive = getNumMylarEdits(id);
				float editsInactive = getNumInactiveEdits(id);
				report.add("Proportion Mylar active (by edits): <b>" + 
						formatPercentage(100*((editsActive)/(editsInactive+editsActive))) + "%</b><br>");
				
				report.add("Elapsed time baseline: " + getTime(id, timeBaseline)
						+ ", active: " + getTime(id, timeMylarActive)
						+ ", inactive: " + getTime(id, timeMylarInactive) + "<br>");

				report.add("Selections baseline: " + getNumBaselineSelections(id)
						+ ", Mylar active: " + getNumMylarSelections(id) 
						+ ", inactive: " + getNumMylarInactiveSelections(id) + "<br>");
				report.add("Edits baseline: " + getNumBaselineEdits(id)
						+ ", Mylar active: " + getNumMylarEdits(id)
						+ ", inactive: " + getNumInactiveEdits(id) + "<br>");

				int numTaskActivations = commandUsageCollector.getCommands().getUserCount(id, TaskActivateAction.ID);
	    		int numTaskDeactivations = commandUsageCollector.getCommands().getUserCount(id, TaskDeactivateAction.ID);
				report.add("Task activations: " + numTaskActivations + ", ");
				report.add("deactivations: " + numTaskDeactivations + "<br>");
//				int numIncrements = commandUsageCollector.getCommands().getUserCount(id, "org.eclipse.mylar.ui.actions.InterestIncrementAction");
//				int numDecrements = commandUsageCollector.getCommands().getUserCount(id, "org.eclipse.mylar.ui.actions.InterestDecrementAction");
//				report.add("Interest increments: " + numIncrements + ", decrements: " + numDecrements + "<br>");
				
				report.addAll(viewUsageCollector.getSummary(id));
				report.add(ReportGenerator.SUMMARY_SEPARATOR);
			} else {
				rejectedUsers++;
			}
		}
		report.add("<h3>Summary</h3>");
		String acceptedSummary = " (based on " + acceptedUsers + " accepted, " + rejectedUsers + " rejected users)";
		float percentage = summaryEditRatioDelta/(float)acceptedUsers;
		String ratioChange = formatPercentage(100*(percentage-1));
		if (percentage >= 1) {
			report.add("Overall edit ratio improved by: " + ratioChange + "% " + acceptedSummary + "<br>");
		} else {
			report.add("Overall edit ratio degraded by: " + ratioChange + "% " + acceptedSummary + "<br>");
		}
		report.add("degraded: " + usersDegraded.size() + ", improved: " + usersImproved.size() + "<br>");
		report.add(ReportGenerator.SUMMARY_SEPARATOR);
		return report;
	}

	public void exportAsCSVFile(String directory) {
		FileWriter writer;
		try {
			writer = new FileWriter(directory + "/mylar-usage.csv");
			writer.write(
					"userid, edit-ratio, " +
					"filtered-explorer, " +
					"edits-active, " +
					"time-baseline, time-active, time-inactive, " +
					"task-activations, task-deactivations, \n");
//					"filtered-explorer, filtered-outline, filtered-problems, ");
					
			for (Iterator it = userIds.iterator(); it.hasNext(); ) {
	    		int id = (Integer)it.next();
	    		
	    		if (acceptUser(id)) {
		    		writer.write(id + ", ");
					float baselineRatio = getBaselineRatio(id);
					float mylarInactiveRatio = getMylarInactiveRatio(id);
					float mylarActiveRatio = getMylarRatio(id);		
					float combinedMylarRatio = mylarInactiveRatio + mylarActiveRatio;
					
					float ratioPercentage = (combinedMylarRatio-baselineRatio) / baselineRatio;
					writer.write(100*ratioPercentage + ", ");
	
					writer.write(viewUsageCollector.getFilteredSelections(id, "org.eclipse.jdt.ui.PackageExplorer") + ", ");
										
					float editsActive = getNumMylarEdits(id);
					float editsInactive = getNumInactiveEdits(id);
					writer.write(100*((editsActive)/(editsInactive+editsActive)) + ", ");
			
					writer.write(getTime(id, timeBaseline) + ", ");
					writer.write(getTime(id, timeMylarActive) + ", ");
					writer.write(getTime(id, timeMylarInactive) + ", ");
					
					int numTaskActivations = commandUsageCollector.getCommands().getUserCount(id, TaskActivateAction.ID);
		    		int numTaskDeactivations = commandUsageCollector.getCommands().getUserCount(id, TaskDeactivateAction.ID);
		    		writer.write(numTaskActivations + ", ");
		    		writer.write(numTaskDeactivations + ", ");
		    		writer.write("\n");
				} 
			}
			writer.close();
		} catch (IOException e) {
			MylarPlugin.fail(e, "could not generate csv file", true);
		}
	}
	
	private String getTime(int id, Map<Integer, Long> timeMap) {
		if (timeMap.containsKey(id)) {
			long timeInSeconds = timeMap.get(id) / 1000;
			long hours, minutes;
			hours = timeInSeconds / 3600;
			timeInSeconds = timeInSeconds - (hours * 3600);
			minutes = timeInSeconds / 60;
			timeInSeconds = timeInSeconds - (minutes * 60);
			return hours + "." + minutes;
		} else {
			return "0";
		}
	}

	public boolean acceptUser(int id) {
		
		if (!numMylarActiveJavaEdits.containsKey(id)) {
			return false;
		} else {
			return 
//				numTaskDeactivations > TASK_DEACTIVATIONS_THRESHOLD
//			 	numMylarActiveJavaEdits.get(id) > (getNumMylarEdits(id)/2)
				getNumBaselineEdits(id) > BASELINE_EDITS_THRESHOLD
				&& getNumMylarEdits(id) > MYLAR_EDITS_THRESHOLD;
		}
	}

	public String formatPercentage(float percentage) {
		String percentageString = "" + percentage;
		int indexOf2ndDecimal = percentageString.indexOf('.')+3;
		if (indexOf2ndDecimal <= percentageString.length()) { 
			percentageString = percentageString.substring(0, indexOf2ndDecimal);
		}
		return percentageString;
	}
	
	public String getStartDate(int id) {
		Calendar start = Calendar.getInstance();
		start.setTime(startDates.get(id));
		return DateUtil.getFormattedDate(start);
	}
	
	public String getEndDate(int id) {
		Calendar end = Calendar.getInstance();
		end.setTime(endDates.get(id));
		return DateUtil.getFormattedDate(end);
	}
	
	public int getNumBaselineSelections(int id) {
		if (baselineSelections.containsKey(id)) {
			return baselineSelections.get(id);
		} else {
			return 0;
		}
	}

	public int getNumBaselineEdits(int id) {
		if (baselineEdits.containsKey(id)) {
			return baselineEdits.get(id);
		} else {
			return 0;
		}
	}
	
	public int getNumMylarEdits(int id) {
		if (mylarEdits.containsKey(id)) {
			return mylarEdits.get(id);
		} else {
			return 0;
		}
	}

	public int getNumInactiveEdits(int id) {
		if (mylarInactiveEdits.containsKey(id)) {
			return mylarInactiveEdits.get(id);
		} else {
			return 0;
		}
	}
	
	public int getNumMylarInactiveSelections(int id) {
		if (mylarInactiveSelections.containsKey(id)) {
			return mylarInactiveSelections.get(id);
		} else {
			return 0;
		}
	}
	
	public int getNumMylarSelections(int id) {
		if (mylarSelections.containsKey(id)) {
			return mylarSelections.get(id);
		} else {
			return 0;
		}
	}
	
	/**
	 * Public for testing.
	 */	
	public float getBaselineRatio(int id) {
		return getEditRatio(id, baselineEdits, baselineSelections);
	}

	public float getMylarInactiveRatio(int id) {
		return getEditRatio(id, mylarInactiveEdits, mylarInactiveSelections);
	}
	
	/**
	 * Public for testing.
	 */	
	public float getMylarRatio(int id) {
		return getEditRatio(id, mylarEdits, mylarSelections);
	}
	
	private float getEditRatio(int id, Map<Integer, Integer> edits, Map<Integer, Integer> selections) {
		if (edits.containsKey(id) && selections.containsKey(id)) {
			return (float)edits.get(id) / (float)selections.get(id); 
		} else {
			return 0f;
		}
	}
}

//String[] REJECTED_IDs = {
//"org.eclipse.mylar.java.ui.editor.MylarCompilationUnitEditor",
//"org.eclipse.jdt.ui.CompilationUnitEditor",
//"org.eclipse.jdt.ui.DefaultTextEditor",
//"org.eclipse.jdt.ui.ClassFileEditor"
//};

//String[] ACCEPTED_EDITORS = {
//"org.eclipse.mylar.java.ui.editor.MylarCompilationUnitEditor",
//"org.eclipse.jdt.ui.CompilationUnitEditor"
//};
//String originId = event.getOriginId();
//if (originId == null) return false;
