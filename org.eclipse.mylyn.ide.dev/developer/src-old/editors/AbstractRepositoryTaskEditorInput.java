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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskAttribute;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskData;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * @deprecated Do not use. This class is pending for removal: see bug 237552.
 */
@Deprecated
public abstract class AbstractRepositoryTaskEditorInput implements IEditorInput {

	protected String toolTipText = "";

	final protected TaskRepository repository;

	final private String taskId;

	private RepositoryTaskData editableTaskData;

	private RepositoryTaskData oldTaskData;

	private Set<RepositoryTaskAttribute> oldEdits;

	protected AbstractRepositoryTaskEditorInput(TaskRepository repository, String taskId) {
		this.taskId = taskId;
		this.repository = repository;
		this.refreshInput();
	}

	/**
	 * Sets the tool tip text for this editor input.
	 * 
	 * @param str
	 *            The new tool tip text.
	 */
	protected void setToolTipText(String str) {
		// 03-20-03 Allows editor to store title (once it is known)
		toolTipText = str;
	}

	public boolean exists() {
		return true;
	}

	/**
	 * returns the new task data
	 */
	public RepositoryTaskData getTaskData() {
		return editableTaskData;
	}

	/**
	 * returns the old task data
	 */
	public RepositoryTaskData getOldTaskData() {
		return oldTaskData;
	}

	public Set<RepositoryTaskAttribute> getOldEdits() {
		return oldEdits;
	}

	public ImageDescriptor getImageDescriptor() {
		return TasksUiImages.REPOSITORY_SMALL;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return getName();
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * @return <code>true</code> if the argument is an editor input on the same bug.
	 */
	@Override
	public abstract boolean equals(Object o);

	public TaskRepository getRepository() {
		return repository;
	}

	protected void setEditableTaskData(RepositoryTaskData editableTaskData) {
//		if (editableTaskData == null) {
//			throw new IllegalArgumentException();
//		}
		this.editableTaskData = editableTaskData;
	}

	protected void setOldTaskData(RepositoryTaskData oldTaskData) {
		this.oldTaskData = oldTaskData;
	}

	/**
	 * @since 2.2
	 */
	protected void setOldEdits(Set<RepositoryTaskAttribute> oldEdits) {
		this.oldEdits = oldEdits;
	}

	public void refreshInput() {
		setEditableTaskData(TasksUiPlugin.getTaskDataStorageManager().getEditableCopy(repository.getRepositoryUrl(),
				taskId));
		setOldTaskData(TasksUiPlugin.getTaskDataStorageManager().getOldTaskData(repository.getRepositoryUrl(), taskId));
		setOldEdits(TasksUiPlugin.getTaskDataStorageManager().getEdits(repository.getRepositoryUrl(), taskId));
	}
}
