/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.bugzilla.deprecated;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskList;
import org.eclipse.mylyn.internal.tasks.core.deprecated.ITaskFactory;
import org.eclipse.mylyn.internal.tasks.core.deprecated.QueryHitCollector;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskData;

public class DummySearchHitProvider extends QueryHitCollector {

	public DummySearchHitProvider(ITaskList tasklist) {
		super(new ITaskFactory() {

			public AbstractTask createTask(RepositoryTaskData taskData, IProgressMonitor monitor) throws CoreException {
				return null;
			}
		});
	}

}
