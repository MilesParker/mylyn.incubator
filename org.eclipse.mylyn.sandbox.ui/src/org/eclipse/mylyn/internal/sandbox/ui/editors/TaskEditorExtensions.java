/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.editors;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author David Green
 */
public class TaskEditorExtensions {

	public static final String REPOSITORY_PROPERTY_EDITOR_EXTENSION = "editorExtension";

	private static Map<String, RegisteredTaskEditorExtension> extensionsById = new HashMap<String, RegisteredTaskEditorExtension>();

	private static Map<String, String> associationByConnectorKind = new HashMap<String, String>();

	private static boolean initialized;

	public static SortedSet<RegisteredTaskEditorExtension> getTaskEditorExtensions() {
		init();
		return new TreeSet<RegisteredTaskEditorExtension>(extensionsById.values());
	}

	public static void addTaskEditorExtension(String id, String name, AbstractTaskEditorExtension extension) {
		Assert.isNotNull(id);
		RegisteredTaskEditorExtension previous = extensionsById.put(id, new RegisteredTaskEditorExtension(extension,
				id, name));
		if (previous != null) {
			StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN, "Duplicate taskEditorExtension id="
					+ id, null));
		}
	}

	public static void addRepositoryAssociation(String connectorKind, String extensionId) {
		if (connectorKind == null || extensionId == null) {
			throw new IllegalArgumentException();
		}
		String previous = associationByConnectorKind.put(connectorKind, extensionId);
		if (previous != null) {
			StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN, String.format(
					"Duplicate association for repository %s: %s replaces %s", connectorKind, extensionId, previous),
					null));
		}
	}

	/**
	 * get a task editor extension for a specific repository
	 * 
	 * @param taskRepository
	 * @return the extension, or null if there is none
	 * 
	 * @see #getDefaultTaskEditorExtension(TaskRepository)
	 */
	public static AbstractTaskEditorExtension getTaskEditorExtension(TaskRepository taskRepository) {
		init();
		String extensionId = getTaskEditorExtensionId(taskRepository);
		if (extensionId != null) {
			RegisteredTaskEditorExtension taskEditorExtension = extensionsById.get(extensionId);
			return taskEditorExtension == null ? null : taskEditorExtension.getExtension();
		}
		return null;
	}

	public static String getTaskEditorExtensionId(TaskRepository taskRepository) {
		init();
		String id = taskRepository.getProperty(REPOSITORY_PROPERTY_EDITOR_EXTENSION);
		if (id == null) {
			id = getDefaultTaskEditorExtensionId(taskRepository);
		}
		return id;
	}

	public static void setTaskEditorExtensionId(TaskRepository repository, String editorExtensionId) {
		repository.setProperty(REPOSITORY_PROPERTY_EDITOR_EXTENSION, editorExtensionId);
	}

	public static String getDefaultTaskEditorExtensionId(TaskRepository taskRepository) {
		init();
		return associationByConnectorKind.get(taskRepository.getConnectorKind());
	}

	/**
	 * get a default task editor extension for a specific repository
	 * 
	 * @param taskRepository
	 * @return the extension, or null if there is none
	 * 
	 * @see #getTaskEditorExtension(TaskRepository)
	 */
	public static AbstractTaskEditorExtension getDefaultTaskEditorExtension(TaskRepository taskRepository) {
		init();
		String extensionId = getDefaultTaskEditorExtensionId(taskRepository);
		if (extensionId != null) {
			RegisteredTaskEditorExtension taskEditorExtension = extensionsById.get(extensionId);
			return taskEditorExtension == null ? null : taskEditorExtension.getExtension();
		}
		return null;
	}

	private static void init() {
		if (!initialized) {
			initialized = true;
			TaskEditorExtensionReader.initExtensions();
		}
	}

	public static class RegisteredTaskEditorExtension implements Comparable<RegisteredTaskEditorExtension> {

		private final String id;

		private final String name;

		private final AbstractTaskEditorExtension extension;

		private RegisteredTaskEditorExtension(AbstractTaskEditorExtension extension, String id, String name) {
			this.extension = extension;
			this.id = id;
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public AbstractTaskEditorExtension getExtension() {
			return extension;
		}

		public int compareTo(RegisteredTaskEditorExtension o) {
			if (o == this) {
				return 0;
			}
			int i = name.compareTo(o.name);
			if (i == 0) {
				i = id.compareTo(o.id);
			}
			return i;
		}
	}

}
