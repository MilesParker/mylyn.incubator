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

package org.eclipse.mylar.monitor.usage.core;

import java.util.List;

import org.eclipse.mylar.context.core.InteractionEvent;

/**
 * @author Mik Kersten
 * @author Leah Findlater
 */
public interface IUsageCollector {

	public String getReportTitle();

	public abstract void consumeEvent(InteractionEvent event, int userId);

	/**
	 * TODO: return report as HTML
	 * 
	 * @return a list corresponding to all of the lines of the report
	 */
	public abstract List<String> getReport();

	/**
	 * Implementors will need to generate a unique filename given the directory
	 * in which to place the file
	 * 
	 * @param directory
	 */
	public abstract void exportAsCSVFile(String directory);
}