/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.internal.monitor.reports.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.internal.monitor.reports.MylarReportsPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for reporting preferences
 * 
 * @author Wesley Coelho
 */
public class MylarReportsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text mylarDataDirectory = null;

	private Button browse = null;

	public MylarReportsPreferencePage() {
		super();
		setPreferenceStore(MylarReportsPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);
		createTaskDirectoryControl(container);
		return container;
	}

	public void init(IWorkbench workbench) {
		// No initialization required
	}

	@Override
	public boolean performOk() {
		String taskDirectory = mylarDataDirectory.getText();
		taskDirectory = taskDirectory.replaceAll("\\\\", "/");
		getPreferenceStore().setValue(MylarReportsPlugin.SHARED_TASK_DATA_ROOT_DIR, taskDirectory);
		return true;
	}

	public void performDefaults() {
		super.performDefaults();

		// IPath rootPath =
		// ResourcesPlugin.getWorkspace().getRoot().getLocation();
		// String taskDirectory = rootPath.toString() + "/" +
		// MylarPlugin.DATA_DIR_NAME;
		mylarDataDirectory.setText(MylarPlugin.getDefault().getDataDirectory());
	}

	private void createTaskDirectoryControl(Composite parent) {
		Group taskDirComposite = new Group(parent, SWT.SHADOW_ETCHED_IN);
		taskDirComposite.setText("Shared Task Data Root Directory");
		taskDirComposite.setLayout(new GridLayout(2, false));
		taskDirComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String taskDirectory = getPreferenceStore().getString(MylarReportsPlugin.SHARED_TASK_DATA_ROOT_DIR);
		if (taskDirectory.trim().equals("")) {
			taskDirectory = MylarPlugin.getDefault().getDataDirectory();
			// getPreferenceStore().getString(MylarPlugin.PREF_DATA_DIR);
		}
		taskDirectory = taskDirectory.replaceAll("\\\\", "/");
		mylarDataDirectory = new Text(taskDirComposite, SWT.BORDER);
		mylarDataDirectory.setText(taskDirectory);
		mylarDataDirectory.setEditable(false);
		mylarDataDirectory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		browse = createButton(taskDirComposite, "Browse...");
		browse.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setText("Folder Selection");
				dialog.setMessage("Specify the root folder where shared task data is stored");
				String dir = mylarDataDirectory.getText();
				dir = dir.replaceAll("\\\\", "/");
				dialog.setFilterPath(dir);

				dir = dialog.open();
				if (dir == null || dir.equals(""))
					return;
				mylarDataDirectory.setText(dir);
			}
		});
	}

	private Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.TRAIL);
		button.setText(text);
		button.setVisible(true);
		return button;
	}
}
