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

package org.eclipse.mylar.monitor.reports.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylar.monitor.reports.IUsageCollector;
import org.eclipse.mylar.monitor.reports.InteractionEventSummary;

/**
 * Container for statistics
 * 
 * @author Mik Kersten
 */
public class UsageStatisticsSummary {

	private List<InteractionEventSummary> singleSummaries = new ArrayList<InteractionEventSummary>();

	private List<IUsageCollector> collectors = new ArrayList<IUsageCollector>();

	public List<InteractionEventSummary> getSingleSummaries() {
		return singleSummaries;
	}

	public void setSingleSummaries(List<InteractionEventSummary> singleSummaries) {
		this.singleSummaries = singleSummaries;
	}

	public void add(int index, IUsageCollector collector) {
		collectors.add(index, collector);
	}

	public void add(IUsageCollector collector) {
		collectors.add(collector);
	}

	public List<IUsageCollector> getCollectors() {
		return collectors;
	}
}
