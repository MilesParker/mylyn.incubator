/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Ken Sueda - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.usage.preferences;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventObfuscator;
import org.eclipse.mylyn.internal.monitor.usage.MonitorPreferenceConstants;
import org.eclipse.mylyn.internal.monitor.usage.StudyParameters;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonColors;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;

/**
 * @author Mik Kersten
 * @author Ken Sueda
 */
public class UsageDataPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String DESCRIPTION = Messages.UsageDataPreferencePage_If_Enabled_Mylyn_Monitors;

	private static final long DAYS_IN_MS = 1000 * 60 * 60 * 24;

	private Button enableMonitoring;

	private Button enableObfuscation;

	private Button enableSubmission;

	private Text logFileText;

	private Text uploadUrl;

	private Text submissionTime;

	private final StudyParameters studyParameters;

	public UsageDataPreferencePage() {
		super();
		setPreferenceStore(UiUsageMonitorPlugin.getDefault().getPreferenceStore());
		setDescription(DESCRIPTION);

		studyParameters = UiUsageMonitorPlugin.getDefault().getStudyParameters();

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayoutFactory.fillDefaults().applyTo(container);

		if (studyParameters.getCustomizingPlugin() != null) {
			Label label = new Label(parent, SWT.NULL);
			label.setText(studyParameters.getCustomizedByMessage());
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));

			if (studyParameters.getMoreInformationUrl() != null) {
				ImageHyperlink link = new ImageHyperlink(parent, SWT.NONE);
				link.setText(Messages.UsageDataPreferencePage_Learn_More);
				link.setForeground(CommonColors.HYPERLINK_WIDGET);
				link.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						openMoreInformaionInBrowser();
					}

				});
			}
		}

		createLogFileSection(container);
		createUsageSection(container);
		updateEnablement();

		Dialog.applyDialogFont(container);
		return container;
	}

	private void openMoreInformaionInBrowser() {
		String moreInformationUrl = studyParameters.getMoreInformationUrl();
		try {
			if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL) {
				try {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					support.getExternalBrowser().openURL(new URL(moreInformationUrl));
				} catch (Exception e) {
					StatusHandler.fail(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
							"Could not open url", e)); //$NON-NLS-1$
				}
			} else {
				IWebBrowser browser = null;
				int flags = 0;
				if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
					flags = IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;

				} else {
					flags = IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				String generatedId = "org.eclipse.mylyn.web.browser-" + Calendar.getInstance().getTimeInMillis(); //$NON-NLS-1$
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags, generatedId, null, null);
				browser.openURL(new URL(moreInformationUrl));
			}
		} catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Browser init error", //$NON-NLS-1$
					"Browser could not be initiated"); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					Messages.UsageDataPreferencePage_Url_Not_Found,
					NLS.bind(Messages.UsageDataPreferencePage_Unable_To_Open_X, moreInformationUrl));
		}
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
		group.setText(Messages.UsageDataPreferencePage_Monitoring);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		enableMonitoring = new Button(group, SWT.CHECK);
		enableMonitoring.setText(Messages.UsageDataPreferencePage_Enable_Logging_To);
		enableMonitoring.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLED));
		enableMonitoring.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore
			}
		});

		String logFilePath = UiUsageMonitorPlugin.getDefault().getMonitorLogFile().getPath();
		logFilePath = logFilePath.replaceAll("\\\\", "/"); //$NON-NLS-1$//$NON-NLS-2$
		logFileText = new Text(group, SWT.BORDER);
		logFileText.setText(logFilePath);
		logFileText.setEditable(false);
		logFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		enableObfuscation = new Button(group, SWT.CHECK);
		enableObfuscation.setText(Messages.UsageDataPreferencePage_Obfuscate_Elements_Using);
		enableObfuscation.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE));

		if (studyParameters.forceObfuscation()) {
			enableObfuscation.setSelection(true);
			enableObfuscation.setEnabled(false);
		}

		Label obfuscationLablel = new Label(group, SWT.NULL);
		obfuscationLablel.setText(InteractionEventObfuscator.ENCRYPTION_ALGORITHM
				+ Messages.UsageDataPreferencePage_Message_Digest_One_Way_Hash);
	}

	private void createUsageSection(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.UsageDataPreferencePage_Usage_Feedback);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NULL);
		label.setText(Messages.UsageDataPreferencePage_Upload_Url);
		uploadUrl = new Text(group, SWT.BORDER);
		uploadUrl.setEditable(false);
		uploadUrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		uploadUrl.setText(studyParameters.getUploadServletUrl());

		Label events = new Label(group, SWT.NULL);
		events.setText(Messages.UsageDataPreferencePage_Total_Events);
		Label logged = new Label(group, SWT.NULL);
		logged.setText("" + getPreferenceStore().getInt(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS)); //$NON-NLS-1$

		events = new Label(group, SWT.NULL);
		events.setText(Messages.UsageDataPreferencePage_Events_Since_Upload);
		logged = new Label(group, SWT.NULL);
		logged.setText("" + getPreferenceStore().getInt(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS_SINCE_LAST_UPLOAD)); //$NON-NLS-1$

		Composite enableSubmissionComposite = new Composite(group, SWT.NULL);
		GridLayout submissionGridLayout = new GridLayout(4, false);
		submissionGridLayout.marginWidth = 0;
		submissionGridLayout.marginHeight = 0;
		enableSubmissionComposite.setLayout(submissionGridLayout);
		enableSubmission = new Button(enableSubmissionComposite, SWT.CHECK);

		enableSubmission.setText(Messages.UsageDataPreferencePage_Enable_Submission_Every);
		enableSubmission.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION));
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
		long submissionFreq = UiUsageMonitorPlugin.DEFAULT_DELAY_BETWEEN_TRANSMITS;
		if (UiUsageMonitorPlugin.getDefault()
				.getPreferenceStore()
				.contains(MonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY)) {
			submissionFreq = getPreferenceStore().getLong(MonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY);
		}
		long submissionFreqInDays = submissionFreq / DAYS_IN_MS;
		submissionTime.setText("" + submissionFreqInDays); //$NON-NLS-1$
		submissionTime.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {

			}
		});
		Label label2 = new Label(enableSubmissionComposite, SWT.NONE);
		label2.setText(Messages.UsageDataPreferencePage_Days);

	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		logFileText.setText(UiUsageMonitorPlugin.getDefault().getMonitorLogFile().getPath());
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE,
				enableObfuscation.getSelection());
		if (enableMonitoring.getSelection()) {
			UiUsageMonitorPlugin.getDefault().startMonitoring();
		} else {
			UiUsageMonitorPlugin.getDefault().stopMonitoring();
		}

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION,
				enableSubmission.getSelection());

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLED,
				enableMonitoring.getSelection());

		long transmitFrequency = UiUsageMonitorPlugin.DEFAULT_DELAY_BETWEEN_TRANSMITS;

		String submissionFrequency = submissionTime.getText();

		try {
			transmitFrequency = Integer.parseInt(submissionFrequency);
			transmitFrequency *= DAYS_IN_MS;
		} catch (NumberFormatException nfe) {
			// do nothing, transmitFrequency will have the default value
		}

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY, transmitFrequency);

		studyParameters.setTransmitPromptPeriod(transmitFrequency);
		return true;
	}

	@Override
	public boolean performCancel() {
		enableMonitoring.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLED));
		enableObfuscation.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE));
		return true;
	}
}
