/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ken Sueda - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.planner;

import java.util.Set;

import org.eclipse.mylyn.internal.tasks.core.AbstractTask;

/**
 * @author Ken Sueda
 */
public interface ITaskCollector {

	public abstract void consumeTask(AbstractTask task);

	public abstract Set<AbstractTask> getTasks();
}
