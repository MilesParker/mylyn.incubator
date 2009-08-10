/*******************************************************************************
 * Copyright (c) 2003 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.bugzilla.ui.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.mylyn.bugzilla.core.BugReport;
import org.eclipse.mylyn.bugzilla.core.IBugzillaBug;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaPlugin;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.internal.bugzilla.core.NewBugModel;
import org.eclipse.mylyn.internal.bugzilla.ui.BugzillaImages;
import org.eclipse.mylyn.internal.bugzilla.ui.OfflineView;
import org.eclipse.mylyn.internal.bugzilla.ui.editor.ExistingBugEditorInput;
import org.eclipse.mylyn.internal.bugzilla.ui.editor.NewBugEditorInput;
import org.eclipse.mylyn.internal.tasklist.MylarTaskListPlugin;
import org.eclipse.mylyn.internal.tasklist.TaskRepository;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * View a bug from the offlineReports menu
 * 
 * @author Mik Kersten (hardening of prototype)
 */
public class ViewOfflineReportAction extends AbstractOfflineReportsAction {

	/** The view to get the result to launch a viewer on */
	private OfflineView view;

	/**
	 * Constructor
	 * 
	 * @param resultsView
	 *            The view to launch a viewer on
	 */
	public ViewOfflineReportAction(OfflineView resultsView) {
		setToolTipText("View Selected Offline Reports");
		setText("View Selected");
		setImageDescriptor(BugzillaImages.OPEN);
		view = resultsView;
	}

	@Override
	public void run() {
		OfflineView.checkWindow();
		List<IBugzillaBug> selectedBugs = view.getSelectedBugs();

		// if there are some selected bugs view the bugs in the editor window
		if (!selectedBugs.isEmpty()) {
			IWorkbenchPage page = BugzillaPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			for (Iterator<IBugzillaBug> it = selectedBugs.iterator(); it.hasNext();) {
				IBugzillaBug bug = it.next();
				if (bug instanceof BugReport) {
					TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getRepository(
							BugzillaPlugin.REPOSITORY_KIND, bug.getRepository());
					if (repository != null) {
						ExistingBugEditorInput editorInput = new ExistingBugEditorInput((BugReport) bug);
						try {
							page.openEditor(editorInput, IBugzillaConstants.EXISTING_BUG_EDITOR_ID);
						} catch (PartInitException e) {
							BugzillaPlugin.log(e);
						}
					}
					continue;
				}
				if (bug instanceof NewBugModel) {
					NewBugEditorInput editorInput = new NewBugEditorInput((NewBugModel) bug);
					try {
						page.openEditor(editorInput, IBugzillaConstants.NEW_BUG_EDITOR_ID);
					} catch (PartInitException e) {
						BugzillaPlugin.log(e);
					}
					continue;
				}
			}
		}
	}
}
