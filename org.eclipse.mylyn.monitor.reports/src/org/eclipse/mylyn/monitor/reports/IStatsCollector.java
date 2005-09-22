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

import java.util.List;

import org.eclipse.mylar.core.InteractionEvent;


/**
 * @author Mik Kersten and Leah Findlater
 */
public interface IStatsCollector {
	public String getLabel();
	
	public abstract void consumeEvent(InteractionEvent event, int userId, String phase);

	public abstract List<String> getSummary();
}