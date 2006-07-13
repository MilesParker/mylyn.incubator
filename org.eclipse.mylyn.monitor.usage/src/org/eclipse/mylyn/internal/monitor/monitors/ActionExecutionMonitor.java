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

package org.eclipse.mylar.internal.monitor.monitors;

import org.eclipse.jface.action.IAction;
import org.eclipse.mylar.context.core.InteractionEvent;
import org.eclipse.mylar.internal.monitor.IActionExecutionListener;
import org.eclipse.mylar.monitor.MylarMonitorPlugin;

/**
 * @author Mik Kersten
 */
public class ActionExecutionMonitor implements IActionExecutionListener {

	public void actionObserved(IAction action) {
		InteractionEvent interactionEvent = InteractionEvent.makeCommand(action.getId(), "");
		MylarMonitorPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}
}
