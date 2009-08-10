///*******************************************************************************
// * Copyright (c) 2004 - 2005 University Of British Columbia and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     University Of British Columbia - initial API and implementation
// *******************************************************************************/
//
//package org.eclipse.mylyn.bugzilla.ui.actions;
//
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.mylyn.bugzilla.core.BugzillaPlugin;
//import org.eclipse.mylyn.bugzilla.ui.BugzillaImages;
//import org.eclipse.mylyn.bugzilla.ui.BugzillaUiPlugin;
//import org.eclipse.mylyn.bugzilla.ui.tasklist.BugzillaTask;
//import org.eclipse.mylyn.tasklist.ITask;
//import org.eclipse.mylyn.tasklist.ITaskHandler;
//import org.eclipse.mylyn.tasklist.MylarTaskListPlugin;
//import org.eclipse.mylyn.tasklist.TaskRepository;
//import org.eclipse.mylyn.tasklist.internal.TaskCategory;
//import org.eclipse.mylyn.tasklist.internal.TaskRepositoryManager;
//import org.eclipse.mylyn.tasklist.ui.views.TaskListView;
//import org.eclipse.ui.IViewActionDelegate;
//import org.eclipse.ui.IViewPart;
//
///**
// * @author Mik Kersten
// * @author Ken Sueda
// */
//public class CreateBugzillaTaskAction extends Action implements IViewActionDelegate {
//	
//	private static final String LABEL = "Add Existing Bugzilla Report";
//
//	public static final String ID = "org.eclipse.mylyn.tasklist.actions.create.bug";
//		
//	public CreateBugzillaTaskAction() {
//		setText(LABEL);
//        setToolTipText(LABEL);
//        setId(ID); 
//        setImageDescriptor(BugzillaImages.TASK_BUGZILLA);
//	} 
//	
//	@Override
//	public void run() {
//		if(TaskListView.getDefault() == null)
//			return;
//
//	    String bugIdString = TaskListView.getDefault().getBugIdFromUser();
//	    int bugId = -1;
//	    try {
//	    	if (bugIdString != null) {
//	    		bugId = Integer.parseInt(bugIdString);
//	    	} else {
//	    		return;
//	    	}
//	    } catch (NumberFormatException nfe) {
//	        TaskListView.getDefault().showMessage("Please enter a valid report number");
//	        return;
//	    }
//	
//	    TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getDefaultRepository(BugzillaPlugin.REPOSITORY_KIND);
//	    ITask newTask = new BugzillaTask(
//	    		TaskRepositoryManager.getHandle(repository.getUrl().toExternalForm(), bugId), 
//	    		"<bugzilla info>", true, true);				
//	    Object selectedObject = ((IStructuredSelection)TaskListView.getDefault().getViewer().getSelection()).getFirstElement();
//    	
//	    ITaskHandler taskHandler = MylarTaskListPlugin.getDefault().getHandlerForElement(newTask);
//	    if(taskHandler != null){
//	    	ITask addedTask = taskHandler.taskAdded(newTask);
//	    	if(addedTask instanceof BugzillaTask){
//		    	BugzillaTask newTask2 = (BugzillaTask)addedTask;
//	    		if(newTask2 == newTask){
//	    			((BugzillaTask)newTask).scheduleDownloadReport();
//	    		} else {
//	    			newTask = newTask2;
//	    			((BugzillaTask)newTask).updateTaskDetails();
//	    		}
//	    	}
//    	} else {
//    		((BugzillaTask)newTask).scheduleDownloadReport();
//    	}
//	    if (selectedObject instanceof TaskCategory) {
//	    	MylarTaskListPlugin.getTaskListManager().moveToCategory(((TaskCategory)selectedObject), newTask);
//	    } else { 
//	        MylarTaskListPlugin.getTaskListManager().moveToRoot(newTask);
//	    }
//	    BugzillaUiPlugin.getDefault().getBugzillaTaskListManager().addToBugzillaTaskRegistry((BugzillaTask)newTask);
//
//	    if(TaskListView.getDefault() != null) {
//			// Make this new task the current selection in the view
//			TaskListView.getDefault().getViewer().setSelection(new StructuredSelection(newTask));
//			TaskListView.getDefault().getViewer().refresh();
//	    }
//	}
//
//	public void init(IViewPart view) {
//	}
//
//	public void run(IAction action) {
//		run();
//	}
//
//	public void selectionChanged(IAction action, ISelection selection) {
//		
//	}
//}
