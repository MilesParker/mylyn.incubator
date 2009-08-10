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

package org.eclipse.mylyn.internal.tasks.ui.deprecated;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskDataStorageManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.deprecated.AbstractLegacyRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.deprecated.AbstractTaskDataHandler;
import org.eclipse.mylyn.internal.tasks.core.deprecated.ITaskFactory;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskData;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.ui.TasksUi;

/**
 * @deprecated Do not use. This class is pending for removal: see bug 237552.
 */
@Deprecated
public class TaskFactory implements ITaskFactory {

	private final AbstractLegacyRepositoryConnector connector;

	private final TaskDataManager synchManager;

	private final TaskRepository repository;

	private final ITaskList taskList;

	private final AbstractTaskDataHandler dataHandler;

	private final boolean updateTasklist;

	private final boolean forced;

	public TaskFactory(TaskRepository repository, boolean updateTasklist, boolean forced) {
		this.repository = repository;
		this.updateTasklist = updateTasklist;
		this.forced = forced;
		connector = (AbstractLegacyRepositoryConnector) TasksUi.getRepositoryManager().getRepositoryConnector(
				repository.getConnectorKind());
		synchManager = TasksUiPlugin.getTaskDataManager();
		taskList = TasksUiInternal.getTaskList();
		//dataManager = TasksUiPlugin.getTaskDataManager();
		dataHandler = connector.getLegacyTaskDataHandler();
	}

	@Deprecated
	public TaskFactory(TaskRepository repository) {
		this(repository, true, false);
	}

	/**
	 * @param updateTasklist
	 *            - synchronize task with the provided taskData
	 * @param forced
	 *            - user requested synchronization
	 * @throws CoreException
	 */
	public AbstractTask createTask(RepositoryTaskData taskData, IProgressMonitor monitor) throws CoreException {
		AbstractTask repositoryTask = (AbstractTask) taskList.getTask(taskData.getRepositoryUrl(), taskData.getTaskId());
		if (repositoryTask == null) {
			repositoryTask = createTaskFromTaskData(connector, repository, taskData, updateTasklist, monitor);
			repositoryTask.setSynchronizationState(SynchronizationState.INCOMING);
			if (updateTasklist) {
				taskList.addTask(repositoryTask);
				synchManager.saveIncoming(repositoryTask, taskData, forced);
			} else {
				synchManager.saveOffline(repositoryTask, taskData);
			}

		} else {
			if (updateTasklist) {
				synchManager.saveIncoming(repositoryTask, taskData, forced);
				connector.updateTaskFromTaskData(repository, repositoryTask, taskData);
				if (dataHandler != null) {
					for (ITask child : repositoryTask.getChildren()) {
						taskList.removeFromContainer(repositoryTask, child);
					}
					Set<String> subTaskIds = dataHandler.getSubTaskIds(taskData);
					if (subTaskIds != null) {
						for (String subId : subTaskIds) {
							if (subId == null || subId.trim().equals("")) {
								continue;
							}
							AbstractTask subTask = createTaskFromExistingId(connector, repository, subId, false,
									new SubProgressMonitor(monitor, 1));
							if (subTask != null) {
								taskList.addTask(subTask, repositoryTask);
							}
						}
					}
				}
			}
		}
		return repositoryTask;
	}

	/**
	 * Creates a new task from the given task data. Does NOT add resulting task to the tasklist
	 */
	private AbstractTask createTaskFromTaskData(AbstractLegacyRepositoryConnector connector, TaskRepository repository,
			RepositoryTaskData taskData, boolean retrieveSubTasks, IProgressMonitor monitor) throws CoreException {
		AbstractTask repositoryTask = null;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			TaskDataStorageManager taskDataManager = TasksUiPlugin.getTaskDataStorageManager();
			if (taskData != null) {
				// Use connector task factory
				repositoryTask = connector.createTask(repository.getRepositoryUrl(), taskData.getTaskId(),
						taskData.getTaskId() + ": " + taskData.getDescription());
				connector.updateTaskFromTaskData(repository, repositoryTask, taskData);
				taskDataManager.setNewTaskData(taskData);

				if (retrieveSubTasks) {
					monitor.beginTask("Creating task", connector.getLegacyTaskDataHandler()
							.getSubTaskIds(taskData)
							.size());
					for (String subId : connector.getLegacyTaskDataHandler().getSubTaskIds(taskData)) {
						if (subId == null || subId.trim().equals("")) {
							continue;
						}
						AbstractTask subTask = createTaskFromExistingId(connector, repository, subId, false,
								new SubProgressMonitor(monitor, 1));
						if (subTask != null) {
							taskList.addTask(subTask, repositoryTask);
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
		return repositoryTask;
	}

	/**
	 * Create new repository task, adding result to tasklist
	 */
	private AbstractTask createTaskFromExistingId(AbstractLegacyRepositoryConnector connector,
			TaskRepository repository, String id, boolean retrieveSubTasks, IProgressMonitor monitor)
			throws CoreException {
		AbstractTask repositoryTask = (AbstractTask) taskList.getTask(repository.getRepositoryUrl(), id);
		if (repositoryTask == null && connector.getLegacyTaskDataHandler() != null) {
			RepositoryTaskData taskData = null;
			taskData = connector.getLegacyTaskDataHandler().getTaskData(repository, id,
					new SubProgressMonitor(monitor, 1));
			if (taskData != null) {
				repositoryTask = createTaskFromTaskData(connector, repository, taskData, retrieveSubTasks,
						new SubProgressMonitor(monitor, 1));
				if (repositoryTask != null) {
					repositoryTask.setSynchronizationState(SynchronizationState.INCOMING);
					taskList.addTask(repositoryTask);
				}
			}
		} // TODO: Handle case similar to web tasks (no taskDataHandler but
		// have tasks)

		return repositoryTask;
	}

}
