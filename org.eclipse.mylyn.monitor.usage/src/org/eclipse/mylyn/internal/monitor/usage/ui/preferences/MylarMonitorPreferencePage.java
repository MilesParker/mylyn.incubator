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

package org.eclipse.mylar.internal.monitor.usage.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mylar.internal.monitor.usage.MylarMonitorPreferenceConstants;
import org.eclipse.mylar.internal.monitor.usage.MylarUsageMonitorPlugin;
import org.eclipse.mylar.monitor.usage.core.InteractionEventObfuscator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Mik Kersten
 * @author Ken Sueda
 */
public class MylarMonitorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String DESCRIPTION = "If enabled the Mylar monitor logs selections, edits, commands, and preference changes. "
			+ "If you would like to help improve the user experience by anonymously sharing non-private "
			+ "parts of this data, enable automatic feedback submission or submit your data via the "
			+ "Usage Summary Wizard.";

	private static final long DAYS_IN_MS = 1000 * 60 * 60 * 24;

	private Button enableMonitoring;

	private Button enableObfuscation;

	private Button enableSubmission;

	private Text logFileText;

	private Text uploadUrl;

	private Text submissionTime;

	public MylarMonitorPreferencePage() {
		super();
		setPreferenceStore(MylarUsageMonitorPlugin.getPrefs());
		setDescription(DESCRIPTION);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		if (MylarUsageMonitorPlugin.getDefault().getCustomizingPlugin() != null) {
			Label label = new Label(parent, SWT.NULL);
			label.setText(MylarUsageMonitorPlugin.getDefault().getCustomizedByMessage());
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		}

		createLogFileSection(container);
		createUsageSection(container);
		updateEnablement();
		return container;
	}

	public void init(IWorkbench workbench) {
		// Nothing to init
	}

	private void updateEnablement() {
		if (!enableMonitoring.getSelection()) {
			logFileText.setEnabled(false);
			enableSubmission.setEnabled(false);
			submissionTime.setEnabled(false);
		} else {
			logFileText.setEnabled(true);
			enableSubmission.setEnabled(true);
			if (!enableSubmission.getSelection()) {
				submissionTime.setEnabled(false);
			} else {
				submissionTime.setEnabled(true);
			}
		}

	}

	private void createLogFileSection(Composite parent) {
		final Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Monitoring");
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		enableMonitoring = new Button(group, SWT.CHECK);
		enableMonitoring.setText("Enable logging to: ");
		enableMonitoring.setSelection(getPreferenceStore().getBoolean(
				MylarMonitorPreferenceConstants.PREF_MONITORING_ENABLED));
		enableMonitoring.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore
			}
		});

		String logFilePath = MylarUsageMonitorPlugin.getDefault().getMonitorLogFile().getPath();
		logFilePath = logFilePath.replaceAll("\\\\", "/");
		logFileText = new Text(group, SWT.BORDER);
		logFileText.setText(logFilePath);
		logFileText.setEditable(false);
		logFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		enableObfuscation = new Button(group, SWT.CHECK);
		enableObfuscation.setText("Obfuscate elements using: ");
		enableObfuscation.setSelection(getPreferenceStore().getBoolean(
				MylarMonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE));
		Label obfuscationLablel = new Label(group, SWT.NULL);
		obfuscationLablel.setText(InteractionEventObfuscator.ENCRYPTION_ALGORITHM + " message digest one-way hash");
	}

	private void createUsageSection(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Usage Feedback");
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NULL);
		label.setText(" Upload URL: ");
		uploadUrl = new Text(group, SWT.BORDER);
		uploadUrl.setEditable(false);
		uploadUrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		uploadUrl.setText(MylarUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl());

		Label events = new Label(group, SWT.NULL);
		events.setText(" Events since upload:");
		Label logged = new Label(group, SWT.NULL);
		logged.setText("" + getPreferenceStore().getInt(MylarMonitorPreferenceConstants.PREF_NUM_USER_EVENTS));

		Composite enableSubmissionComposite = new Composite(group, SWT.NULL);
		GridLayout submissionGridLayout = new GridLayout(4, false);
		submissionGridLayout.marginWidth = 0;
		submissionGridLayout.marginHeight = 0;
		enableSubmissionComposite.setLayout(submissionGridLayout);
		enableSubmission = new Button(enableSubmissionComposite, SWT.CHECK);

		enableSubmission.setText("Enable submission every");
		enableSubmission.setSelection(getPreferenceStore().getBoolean(
				MylarMonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION));
		enableSubmission.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		submissionTime = new Text(enableSubmissionComposite, SWT.BORDER | SWT.RIGHT);
		GridData gridData = new GridData();
		gridData.widthHint = 15;
		submissionTime.setLayoutData(gridData);
		long submissionFreq = MylarUsageMonitorPlugin.DEFAULT_DELAY_BETWEEN_TRANSMITS;
		if (MylarUsageMonitorPlugin.getPrefs().contains(
				MylarMonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY)) {
			submissionFreq = MylarUsageMonitorPlugin.getPrefs().getLong(
					MylarMonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY);
		}
		long submissionFreqInDays = (long) ((double) submissionFreq) / DAYS_IN_MS;
		submissionTime.setText("" + submissionFreqInDays);
		submissionTime.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {

			}
		});
		Label label2 = new Label(enableSubmissionComposite, SWT.NONE);
		label2.setText("days");

	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		logFileText.setText(MylarUsageMonitorPlugin.getDefault().getMonitorLogFile().getPath());
	}

	@Override
	public boolean performOk() {
		MylarUsageMonitorPlugin.getPrefs().setValue(MylarMonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE,
				enableObfuscation.getSelection());
		if (enableMonitoring.getSelection()) {
			MylarUsageMonitorPlugin.getDefault().startMonitoring();
		} else {
			MylarUsageMonitorPlugin.getDefault().stopMonitoring();
		}

		MylarUsageMonitorPlugin.getPrefs().setValue(MylarMonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION,
				enableSubmission.getSelection());

		long transmitFrequency = MylarUsageMonitorPlugin.DEFAULT_DELAY_BETWEEN_TRANSMITS;

		String submissionFrequency = submissionTime.getText();

		try {
			transmitFrequency = Integer.parseInt(submissionFrequency);
			// we
			transmitFrequency *= DAYS_IN_MS;
		} catch (NumberFormatException nfe) {
			// do nothing, transmitFrequency will have the default value
		}

		MylarUsageMonitorPlugin.getPrefs().setValue(MylarMonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY,
				transmitFrequency);

		MylarUsageMonitorPlugin.getDefault().getStudyParameters().setTransmitPromptPeriod(transmitFrequency);
		return true;
	}

	@Override
	public boolean performCancel() {
		enableMonitoring.setSelection(getPreferenceStore().getBoolean(
				MylarMonitorPreferenceConstants.PREF_MONITORING_ENABLED));
		enableObfuscation.setSelection(getPreferenceStore().getBoolean(
				MylarMonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE));
		// userStudyId.setStringValue(MylarUsageMonitorPlugin.getDefault().getPreferenceStore().getInt(
		// MylarUsageMonitorPlugin.PREF_USER_ID)
		// + "");
		return true;
	}
}
