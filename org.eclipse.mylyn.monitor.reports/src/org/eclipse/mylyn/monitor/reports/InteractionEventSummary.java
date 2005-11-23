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

package org.eclipse.mylar.monitor.reports;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Leah Findlater and Mik Kersten
 * 
 * Stores the type, ID, name, and usage count of a single function that can be
 * stored in the Taskscape.
 */
public class InteractionEventSummary {
	private String type;

	private String name;

	private int usageCount;

	private float interestContribution;

	private String delta;

	private Set<Integer> userIds = new HashSet<Integer>();

	public InteractionEventSummary(String type, String name, int usageCount) {
		this.type = type;
		this.name = name;
		this.usageCount = usageCount;

	}

	public InteractionEventSummary() {
		type = "";
		name = "";
		usageCount = 0;
	}

	public InteractionEventSummary(InteractionEventSummary another) {
		this.type = another.type;
		this.name = another.name;
		this.usageCount = another.usageCount;
		this.userIds.addAll(another.getUserIds());
	}

	public void combine(InteractionEventSummary another) {
		this.usageCount = this.usageCount + another.getUsageCount();
		this.userIds.addAll(another.getUserIds());
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return Returns the usageCount.
	 */
	public int getUsageCount() {
		return usageCount;
	}

	/**
	 * @param usageCount
	 *            The usageCount to set.
	 */
	public void setUsageCount(int usageCount) {
		this.usageCount = usageCount;
	}

	public float getInterestContribution() {
		return interestContribution;
	}

	public void setInterestContribution(float interestContribution) {
		this.interestContribution = interestContribution;
	}

	public String getDelta() {
		if ("null".equals(delta)) {
			return "";
		} else {
			return delta;
		}
	}

	public void setDelta(String delta) {
		this.delta = delta;
	}

	public Set<Integer> getUserIds() {
		return userIds;
	}

	public void setUserIds(Set<Integer> userIds) {
		this.userIds = userIds;
	}

	public void addUserId(int userId) {
		if (!userIds.contains(userId)) {
			this.userIds.add(userId);
		}
	}
}
