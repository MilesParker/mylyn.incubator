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
package org.eclipse.mylar.monitor.usage.core.collectors;

import org.eclipse.mylar.context.core.InteractionEvent;
import org.eclipse.mylar.internal.tasks.ui.actions.TaskActivateAction;
import org.eclipse.mylar.internal.tasks.ui.actions.TaskDeactivateAction;

public abstract class MylarUsageDetector {

	public static boolean isAMylarActivateCommand(InteractionEvent event) {
		if (event.getKind().equals(InteractionEvent.Kind.COMMAND)) {
			if (event.getOriginId().equals(TaskActivateAction.ID)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAMylarDeactivateCommand(InteractionEvent event) {
		if (event.getKind().equals(InteractionEvent.Kind.COMMAND)) {
			if (event.getOriginId().equals(TaskDeactivateAction.ID)) {
				return true;
			}
		}
		return false;
	}
}
