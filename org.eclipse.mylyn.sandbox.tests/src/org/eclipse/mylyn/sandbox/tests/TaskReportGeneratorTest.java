/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.sandbox.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryQuery;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaTask;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.internal.sandbox.ui.planner.CompletedTaskCollector;
import org.eclipse.mylyn.internal.sandbox.ui.planner.TaskReportGenerator;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.ui.TaskListManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Mik Kersten
 * @author Rob Elves
 */
public class TaskReportGeneratorTest extends TestCase {

	private TaskListManager manager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		manager = TasksUiPlugin.getTaskListManager();
		manager.resetTaskList();
		assertEquals(0, manager.getTaskList().getAllTasks().size());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		manager.resetTaskList();
		TasksUiPlugin.getTaskListManager().saveTaskList();
//		TasksUiPlugin.getDefault().getTaskListSaveManager().saveTaskList(true);
		assertEquals(0, manager.getTaskList().getAllTasks().size());
	}

	public void testCompletedTasksRetrieved() throws InvocationTargetException, InterruptedException {
		AbstractTask task1 = TasksUiInternal.createNewLocalTask("task 1");
		manager.getTaskList().addTask(task1);

		CompletedTaskCollector collector = new CompletedTaskCollector(new Date(0), new Date());
		TaskReportGenerator generator = new TaskReportGenerator(manager.getTaskList());
		generator.addCollector(collector);
		generator.run(new NullProgressMonitor());
		assertEquals(0, generator.getAllCollectedTasks().size());

		task1.setCompleted(true);
		collector = new CompletedTaskCollector(new Date(0), new Date());
		generator = new TaskReportGenerator(manager.getTaskList());
		generator.addCollector(collector);
		generator.run(new NullProgressMonitor());
		assertEquals(1, generator.getAllCollectedTasks().size());
		assertEquals(task1, generator.getAllCollectedTasks().get(0));
	}

	public void testCompletedTasksDateBoundsRetrieved() throws InvocationTargetException, InterruptedException {
		AbstractTask task1 = TasksUiInternal.createNewLocalTask("task 1");
		manager.getTaskList().addTask(task1);
		task1.setCompleted(true);
		Thread.sleep(1000);
		long now = new Date().getTime();

		CompletedTaskCollector collector = new CompletedTaskCollector(new Date(now), new Date());
		TaskReportGenerator generator = new TaskReportGenerator(manager.getTaskList());
		generator.addCollector(collector);
		generator.run(new NullProgressMonitor());
		assertEquals(0, generator.getAllCollectedTasks().size());

		generator = new TaskReportGenerator(manager.getTaskList());
		collector = new CompletedTaskCollector(new Date(now - 8000), new Date());
		generator.addCollector(collector);
		generator.run(new NullProgressMonitor());
		assertEquals(1, generator.getAllCollectedTasks().size());
		assertEquals(task1, generator.getAllCollectedTasks().get(0));
	}

	public void testCompletedBugzillaTasksRetrieved() throws InvocationTargetException, InterruptedException {
		TaskRepository repository = new TaskRepository(BugzillaCorePlugin.REPOSITORY_KIND,
				IBugzillaConstants.ECLIPSE_BUGZILLA_URL);
		TasksUiPlugin.getRepositoryManager().addRepository(repository);
		BugzillaTask task1 = new BugzillaTask(IBugzillaConstants.ECLIPSE_BUGZILLA_URL, "1", "bugzillatask 1");
		manager.getTaskList().addTask(task1);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 5);
		CompletedTaskCollector collector = new CompletedTaskCollector(new Date(0), cal.getTime());
		TaskReportGenerator generator = new TaskReportGenerator(manager.getTaskList());
		generator.addCollector(collector);
		generator.run(new NullProgressMonitor());
		assertEquals(0, generator.getAllCollectedTasks().size());

		task1.setCompleted(true);
		generator.run(new NullProgressMonitor());
		assertEquals(1, generator.getAllCollectedTasks().size());
		assertEquals(task1, generator.getAllCollectedTasks().get(0));
		TasksUiPlugin.getRepositoryManager().removeRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
		manager.resetTaskList();
	}

	public void testCompletedTasksInCategoryRetrieved() throws InvocationTargetException, InterruptedException {
		AbstractTask task1 = TasksUiInternal.createNewLocalTask("task 1");
		manager.getTaskList().addTask(task1);
		task1.setCompleted(true);
		TaskCategory cat1 = new TaskCategory("TaskReportGeneratorTest Category");
		manager.getTaskList().addCategory(cat1);

		Set<AbstractTaskContainer> catagories = new HashSet<AbstractTaskContainer>();
		catagories.add(cat1);

		CompletedTaskCollector collector = new CompletedTaskCollector(new Date(0), new Date());
		TaskReportGenerator generator = new TaskReportGenerator(manager.getTaskList(), catagories);
		generator.addCollector(collector);
		generator.run(new NullProgressMonitor());
		assertEquals(0, generator.getAllCollectedTasks().size());

		manager.getTaskList().addTask(task1, cat1);

		generator.run(new NullProgressMonitor());
		assertEquals(1, generator.getAllCollectedTasks().size());
		assertEquals(task1, generator.getAllCollectedTasks().get(0));
	}

	public void testCompletedBugzillaTasksInCategoryRetrieved() throws InvocationTargetException, InterruptedException {
		BugzillaTask task1 = new BugzillaTask("repo", "1", "task 1");
		manager.getTaskList().addTask(task1);
		task1.setCompleted(true);
		TaskCategory cat1 = new TaskCategory("TaskReportGeneratorTest Category");
		manager.getTaskList().addCategory(cat1);

		Set<AbstractTaskContainer> catagories = new HashSet<AbstractTaskContainer>();
		catagories.add(cat1);

		CompletedTaskCollector collector = new CompletedTaskCollector(new Date(0), new Date());
		TaskReportGenerator generator = new TaskReportGenerator(manager.getTaskList(), catagories);
		generator.addCollector(collector);
		generator.run(new NullProgressMonitor());
		assertEquals(0, generator.getAllCollectedTasks().size());

		manager.getTaskList().addTask(task1, cat1);

		generator.run(new NullProgressMonitor());
		assertEquals(1, generator.getAllCollectedTasks().size());
		assertEquals(task1, generator.getAllCollectedTasks().get(0));
	}

	public void testCompletedBugzillaTasksInQueryRetrieved() throws InvocationTargetException, InterruptedException {
		BugzillaTask task1 = new BugzillaTask("repo", "1", "task 1");
		manager.getTaskList().addTask(task1);
		task1.setCompleted(false);

		BugzillaRepositoryQuery bugQuery = new BugzillaRepositoryQuery("repositoryUrl", "queryUrl",
				"TaskReportGeneratorBugzillaQueryCategory");

		manager.getTaskList().addQuery(bugQuery);

		Set<AbstractTaskContainer> catagories = new HashSet<AbstractTaskContainer>();
		catagories.add(bugQuery);
		Calendar future = Calendar.getInstance();
		future.add(Calendar.MINUTE, 1);
		CompletedTaskCollector collector = new CompletedTaskCollector(new Date(0), future.getTime());
		TaskReportGenerator generator = new TaskReportGenerator(manager.getTaskList(), catagories);
		generator.addCollector(collector);
		generator.run(new NullProgressMonitor());
		assertEquals(0, generator.getAllCollectedTasks().size());

		manager.getTaskList().addTask(task1, bugQuery);

		generator.run(new NullProgressMonitor());
		assertEquals(0, generator.getAllCollectedTasks().size());

		task1.setCompleted(true);

		generator.run(new NullProgressMonitor());
		assertEquals(1, generator.getAllCollectedTasks().size());
		assertEquals(task1, generator.getAllCollectedTasks().get(0));
	}

}
