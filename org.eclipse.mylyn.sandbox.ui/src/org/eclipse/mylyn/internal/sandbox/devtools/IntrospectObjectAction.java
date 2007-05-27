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

package org.eclipse.mylar.internal.sandbox.devtools;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IInteractionElement;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author Mik Kersten
 */
public class IntrospectObjectAction implements IViewActionDelegate {

	private ISelection currentSelection;

	public void init(IViewPart view) {

	}

	public void run(IAction action) {
		if (currentSelection instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) currentSelection;
			Object object = selection.getFirstElement();

			String text = "Object class: " + object.getClass() + "\n\n";

			try {
				AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(object);
				IInteractionElement node = ContextCorePlugin.getContextManager().getElement(bridge.getHandleIdentifier(object));
				if (node != null) {
					text += "Interest value: " + node.getInterest().getValue() + "\n";
					text += node.getInterest().toString();
				}
			} catch (Throwable t) {
				text += "<no structure bridge>";
			}
			
			if (object instanceof IAdaptable) {
				Object resourceAdapter = ((IAdaptable)object).getAdapter(IResource.class);
				if (resourceAdapter != null) {
					text += "\nResource adapter: " + ((IResource)resourceAdapter).getFullPath().toOSString();
				}
			}

			if (object instanceof AbstractRepositoryTask || object instanceof AbstractQueryHit) {
				
				AbstractRepositoryTask task;
				if (object instanceof AbstractRepositoryTask) {
					task = (AbstractRepositoryTask)object;
				} else {
					task = ((AbstractQueryHit)object).getCorrespondingTask();
				}
				if (task != null) {
					TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(task.getRepositoryKind(), task.getRepositoryUrl());
					text += "\nLast time in SYNCHRONIZED state: " + task.getLastSyncDateStamp();
					text += "\nRepository synch time stamp: " + repository.getSyncTimeStamp();
					text += "\nSync state: "+ task.getSyncState();
					text += "\nParent: "+ task.getContainer();
					if(task.getChildren() != null && !task.getChildren().isEmpty()){
						text += "\nChildren: ";
						for (ITask subTask : task.getChildren()) {
							text += "\n"+subTask;							
						}
					}
				}
			}
			text += "\n\nNum tasks: " + TasksUiPlugin.getTaskListManager().getTaskList().getAllTasks().size();
			text += "\nNum queries: " + TasksUiPlugin.getTaskListManager().getTaskList().getQueries().size();	
			
			MessageDialog.openInformation(null, "Mylar Sandbox", text);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.currentSelection = selection;
	}

}
