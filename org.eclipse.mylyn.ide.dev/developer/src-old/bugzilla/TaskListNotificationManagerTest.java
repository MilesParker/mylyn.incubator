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

import junit.framework.TestCase;

import org.eclipse.mylyn.bugzilla.deprecated.BugzillaRepositoryQuery;
import org.eclipse.mylyn.bugzilla.deprecated.BugzillaTask;
import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotification;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.ui.TaskListNotificationManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;

/**
 * @author Rob Elves
 * @deprecated
 */
@Deprecated
public class TaskListNotificationManagerTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testTaskListNotificationReminder() throws InterruptedException {

		AbstractTask task0 = new LocalTask("0", "t0 - test 0");
		AbstractTask task1 = new LocalTask("1", "t1 - test 1");
		AbstractTask task2 = new LocalTask("2", "t2 - test 2");

		task0.setScheduledForDate(TaskActivityUtil.getCurrentWeek().getToday().previous());
		task1.setScheduledForDate(TaskActivityUtil.getCurrentWeek().getToday().previous());
		task2.setScheduledForDate(TaskActivityUtil.getCurrentWeek().getToday().previous());

		TasksUiPlugin.getTaskList().addTask(task0);
		TasksUiPlugin.getTaskList().addTask(task1);
		TasksUiPlugin.getTaskList().addTask(task2);

		TaskListNotificationManager notificationManager = TasksUiPlugin.getTaskListNotificationManager();
		notificationManager.collectNotifications();

		task0 = TasksUiPlugin.getTaskList().getTask("local-0");
		assertNotNull(task0);
		assertTrue(task0.isReminded());
		task1 = TasksUiPlugin.getTaskList().getTask("local-1");
		assertNotNull(task1);
		assertTrue(task1.isReminded());
		task2 = TasksUiPlugin.getTaskList().getTask("local-2");
		assertNotNull(task2);
		assertTrue(task2.isReminded());

	}

	public void testTaskListNotificationIncoming() {

		TaskRepository repository = new TaskRepository("bugzilla", "https://bugs.eclipse.org/bugs");
		TasksUiPlugin.getRepositoryManager().addRepository(repository);
		AbstractTask task = new BugzillaTask("https://bugs.eclipse.org/bugs", "142891", "label");
		assertEquals(SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
		assertFalse(task.isNotified());
		TasksUiPlugin.getTaskList().addTask(task);
		TaskListNotificationManager notificationManager = TasksUiPlugin.getTaskListNotificationManager();
		notificationManager.collectNotifications();
		TaskListNotification notification = new TaskListNotification(task);
		notification.setDescription("Unread task");
		assertTrue(notificationManager.getNotifications().contains(notification));
		task = TasksUiPlugin.getTaskList().getTask("https://bugs.eclipse.org/bugs-142891");
		assertNotNull(task);
		assertTrue(task.isNotified());
	}

	public void testTaskListNotificationQueryIncoming() {
		BugzillaTask hit = new BugzillaTask("https://bugs.eclipse.org/bugs", "1", "summary");
		assertFalse(hit.isNotified());
		BugzillaRepositoryQuery query = new BugzillaRepositoryQuery("https://bugs.eclipse.org/bugs", "queryUrl",
				"summary");
		TasksUiPlugin.getTaskList().addQuery(query);
		TasksUiPlugin.getTaskList().addTask(hit, query);

		TaskListNotificationManager notificationManager = TasksUiPlugin.getTaskListNotificationManager();
		assertFalse(hit.isNotified());
		notificationManager.collectNotifications();
		for (AbstractNotification notification : notificationManager.getNotifications()) {
			notification.getLabel().equals(hit.getSummary());
		}
		//assertTrue(notificationManager.getNotifications().contains(new TaskListNotificationQueryIncoming(hit)));
		assertTrue(hit.isNotified());
	}

	public void testTaskListNotificationQueryIncomingRepeats() {
		TaskTestUtil.resetTaskList();
		BugzillaTask hit = new BugzillaTask("https://bugs.eclipse.org/bugs", "1", "summary");
		String hitHandle = hit.getHandleIdentifier();
		assertFalse(hit.isNotified());
		BugzillaRepositoryQuery query = new BugzillaRepositoryQuery("https://bugs.eclipse.org/bugs", "queryUrl",
				"summary");
		TasksUiPlugin.getTaskList().addQuery(query);
		TasksUiPlugin.getTaskList().addTask(hit, query);
		TaskListNotificationManager notificationManager = TasksUiPlugin.getTaskListNotificationManager();
		notificationManager.collectNotifications();
		for (AbstractNotification notification : notificationManager.getNotifications()) {
			notification.getLabel().equals(hit.getSummary());
		}
		//assertTrue(notificationManager.getNotifications().iterator().next().equals(new TaskListNotificationQueryIncoming(hit)));
		assertTrue(hit.isNotified());

		TasksUiPlugin.getTaskListManager().saveTaskList();
		TaskTestUtil.resetTaskList();
		assertEquals(0, TasksUiPlugin.getTaskList().getQueries().size());
		assertTrue(TasksUiPlugin.getTaskListManager().readExistingOrCreateNewList());
		assertEquals(1, TasksUiPlugin.getTaskList().getQueries().size());
		BugzillaTask hitLoaded = (BugzillaTask) TasksUiPlugin.getTaskList().getTask(hitHandle);
		assertNotNull(hitLoaded);
		assertTrue(hitLoaded.isNotified());
	}

}
