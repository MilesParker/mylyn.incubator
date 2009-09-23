/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.tasks.tests.performance;

import java.io.File;

import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TransferList;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.tests.TaskTestUtil;
import org.eclipse.test.performance.PerformanceTestCase;

public class TaskListPerformanceTest extends PerformanceTestCase {

	private static final String TASK_LIST_4000 = "testdata/performance/tasklist-4000.xml.zip";

	private TaskList taskList;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		taskList = TasksUiPlugin.getTaskList();
		taskList.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		taskList.reset();
	}

	public void testReadTasksWith4000Tasks() throws Exception {
		final File file = TaskTestUtil.getLocalFile(TASK_LIST_4000);
		final TaskListExternalizer taskListWriter = TasksUiPlugin.getDefault().createTaskListExternalizer();

		for (int i = 0; i < 10; i++) {
			startMeasuring();
			taskListWriter.readTaskList(new TransferList(), file);
			stopMeasuring();
			taskList.reset();
		}

		commitMeasurements();
		assertPerformance();
	}

	public void testReadTaskListWith4000Tasks() throws Exception {
		final File file = TaskTestUtil.getLocalFile(TASK_LIST_4000);
		final TaskListExternalizer taskListWriter = TasksUiPlugin.getDefault().createTaskListExternalizer();

		for (int i = 0; i < 10; i++) {
			startMeasuring();
			taskListWriter.readTaskList(new TransferList(), file);
			stopMeasuring();
			taskList.reset();
		}

		commitMeasurements();
		assertPerformance();
	}

}
