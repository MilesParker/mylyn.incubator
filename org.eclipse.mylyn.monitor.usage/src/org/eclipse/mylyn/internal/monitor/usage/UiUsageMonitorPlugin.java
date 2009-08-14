/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.usage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.internal.monitor.ui.ActionExecutionMonitor;
import org.eclipse.mylyn.internal.monitor.ui.ActivityChangeMonitor;
import org.eclipse.mylyn.internal.monitor.ui.KeybindingCommandMonitor;
import org.eclipse.mylyn.internal.monitor.ui.MenuCommandMonitor;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.monitor.ui.PerspectiveChangeMonitor;
import org.eclipse.mylyn.internal.monitor.ui.PreferenceChangeMonitor;
import org.eclipse.mylyn.internal.monitor.ui.WindowChangeMonitor;
import org.eclipse.mylyn.internal.monitor.usage.wizards.NewUsageSummaryEditorWizard;
import org.eclipse.mylyn.monitor.core.IInteractionEventListener;
import org.eclipse.mylyn.monitor.ui.AbstractCommandMonitor;
import org.eclipse.mylyn.monitor.ui.IActionExecutionListener;
import org.eclipse.mylyn.monitor.ui.IMonitorLifecycleListener;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 * @author Shawn Minto
 */
public class UiUsageMonitorPlugin extends AbstractUIPlugin {

	// TODO make ids be per-study
	public static final String PREF_USER_ID = "org.eclipse.mylyn.user.id";

	public static final long HOUR = 60 * 60 * 1000;

	public static final long DAY = HOUR * 24;

	public static final long DELAY_ON_USER_REQUEST = 5 * DAY;

	public static final long DEFAULT_DELAY_DAYS_BETWEEN_TRANSMITS = 21;

	public static final long DEFAULT_DELAY_BETWEEN_TRANSMITS = DEFAULT_DELAY_DAYS_BETWEEN_TRANSMITS * DAY;

	public static final String MONITOR_LOG_NAME = "monitor-log";

	public static final String ID_PLUGIN = "org.eclipse.mylyn.monitor.usage";

	private InteractionEventLogger interactionLogger;

	private PreferenceChangeMonitor preferenceMonitor;

	private PerspectiveChangeMonitor perspectiveMonitor;

	private ActivityChangeMonitor activityMonitor;

	private MenuCommandMonitor menuMonitor;

	private WindowChangeMonitor windowMonitor;

	private KeybindingCommandMonitor keybindingCommandMonitor;

	private static UiUsageMonitorPlugin plugin;

	private final List<IActionExecutionListener> actionExecutionListeners = new ArrayList<IActionExecutionListener>();

	private final List<AbstractCommandMonitor> commandMonitors = new ArrayList<AbstractCommandMonitor>();

	private static Date lastTransmit = null;

	private static boolean performingUpload = false;

	private StudyParameters studyParameters = new StudyParameters();

	private final ListenerList lifecycleListeners = new ListenerList();

	private final UsageUploadManager uploadManager = new UsageUploadManager();

	public UsageUploadManager getUploadManager() {
		return uploadManager;
	}

	public static class UiUsageMonitorStartup implements IStartup {

		public void earlyStartup() {
			// everything happens on normal start
		}
	}

	private final IWindowListener WINDOW_LISTENER = new IWindowListener() {
		public void windowActivated(IWorkbenchWindow window) {
		}

		public void windowDeactivated(IWorkbenchWindow window) {
		}

		public void windowClosed(IWorkbenchWindow window) {
			if (window.getShell() != null) {
				window.getShell().removeShellListener(SHELL_LISTENER);
			}
		}

		public void windowOpened(IWorkbenchWindow window) {
			if (window.getShell() != null && !PlatformUI.getWorkbench().isClosing()) {
				window.getShell().addShellListener(SHELL_LISTENER);
			}
		}
	};

	private final ShellListener SHELL_LISTENER = new ShellListener() {

		public void shellDeactivated(ShellEvent arg0) {
			if (!isPerformingUpload() && ContextCorePlugin.getDefault() != null) {
				for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
					listener.stopMonitoring();
				}
			}
		}

		public void shellActivated(ShellEvent arg0) {
//			if (!MonitorUiPlugin.getDefault().suppressConfigurationWizards() && ContextCorePlugin.getDefault() != null) {
//				checkForStatisticsUpload();
//			}
			if (!isPerformingUpload() && ContextCorePlugin.getDefault() != null) {
				for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
					listener.startMonitoring();
				}
			}
		}

		public void shellDeiconified(ShellEvent arg0) {
		}

		public void shellIconified(ShellEvent arg0) {
		}

		public void shellClosed(ShellEvent arg0) {
		}
	};

	private LogMoveUtility logMoveUtility;

	public UiUsageMonitorPlugin() {
		plugin = this;

	}

	private void initDefaultPrefs() {
		getPreferenceStore().setDefault(MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE, true);

		if (!getPreferenceStore().contains(MonitorPreferenceConstants.PREF_MONITORING_INITIALLY_ENABLED)) {
			getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_INITIALLY_ENABLED, true);
			getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLED, true);
		}

		if (!getPreferenceStore().contains(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION_INITITALLY_ENABLED)) {
			getPreferenceStore().setValue(
					MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION_INITITALLY_ENABLED, true);
			getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION, true);

		}

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_STARTED, false);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		initDefaultPrefs();
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					// ------- moved from synch start
					UiUsageMonitorExtensionPointReader uiUsageMonitorExtensionPointReader = new UiUsageMonitorExtensionPointReader();
					studyParameters = uiUsageMonitorExtensionPointReader.getStudyParameters();

					if (studyParameters == null || studyParameters.isEmpty()) {

						initializeDefaultStudyParameters();

					}

					if (preferenceMonitor == null) {
						preferenceMonitor = new PreferenceChangeMonitor();
					}

					interactionLogger = new InteractionEventLogger(getMonitorLogFile());
					perspectiveMonitor = new PerspectiveChangeMonitor();
					activityMonitor = new ActivityChangeMonitor();
					windowMonitor = new WindowChangeMonitor();
					menuMonitor = new MenuCommandMonitor();
					keybindingCommandMonitor = new KeybindingCommandMonitor();

					// ------- moved from synch start

					if (getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED)) {
						startMonitoring();
					}

					if (plugin.getPreferenceStore().contains(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE)) {
						lastTransmit = new Date(plugin.getPreferenceStore().getLong(
								MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE));
					} else {
						lastTransmit = new Date();
						plugin.getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE,
								lastTransmit.getTime());
					}
				} catch (Throwable t) {
					StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
							"Monitor failed to start", t));
				}
			}
		});
	}

	public void startMonitoring() {
		if (studyParameters == null || !studyParameters.isComplete()) {
			return;
		}
		if (getPreferenceStore().contains(MonitorPreferenceConstants.PREF_MONITORING_STARTED)) {
			return;
		}
		interactionLogger.startMonitoring();
		for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
			listener.startMonitoring();
		}

		IWorkbench workbench = PlatformUI.getWorkbench();
		MonitorUi.addInteractionListener(interactionLogger);
		getCommandMonitors().add(keybindingCommandMonitor);

		getActionExecutionListeners().add(new ActionExecutionMonitor());
		workbench.addWindowListener(WINDOW_LISTENER);
		for (IWorkbenchWindow w : MonitorUiPlugin.getDefault().getMonitoredWindows()) {
			if (w.getShell() != null) {
				w.getShell().addShellListener(SHELL_LISTENER);
			}
		}

		if (logMoveUtility == null) {
			logMoveUtility = new LogMoveUtility();
		}
		logMoveUtility.start();

		MonitorUiPlugin.getDefault().addWindowPerspectiveListener(perspectiveMonitor);
		workbench.getActivitySupport().getActivityManager().addActivityManagerListener(activityMonitor);
		workbench.getDisplay().addFilter(SWT.Selection, menuMonitor);
		workbench.addWindowListener(windowMonitor);

		// installBrowserMonitor(workbench);

		for (Object listener : lifecycleListeners.getListeners()) {
			((IMonitorLifecycleListener) listener).startMonitoring();
		}

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_STARTED, true);
	}

	public void addMonitoredPreferences(Preferences preferences) {
		if (preferenceMonitor == null) {
			preferenceMonitor = new PreferenceChangeMonitor();
		}
		preferences.addPropertyChangeListener(preferenceMonitor);
	}

	public void removeMonitoredPreferences(Preferences preferences) {
		if (preferenceMonitor != null) {
			preferences.removePropertyChangeListener(preferenceMonitor);
		} else {
			StatusHandler.log(new Status(IStatus.WARNING, UiUsageMonitorPlugin.ID_PLUGIN,
					"UI Usage Monitor not started", new Exception()));
		}
	}

	public boolean isObfuscationEnabled() {
		return UiUsageMonitorPlugin.getDefault().getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE)
				|| (studyParameters != null && studyParameters.forceObfuscation());
	}

	public void stopMonitoring() {
		if (!getPreferenceStore().contains(MonitorPreferenceConstants.PREF_MONITORING_STARTED)) {
			return;
		}

		for (Object listener : lifecycleListeners.getListeners()) {
			((IMonitorLifecycleListener) listener).stopMonitoring();
		}

		for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
			listener.stopMonitoring();
		}

		IWorkbench workbench = PlatformUI.getWorkbench();
		MonitorUi.removeInteractionListener(interactionLogger);

		getCommandMonitors().remove(keybindingCommandMonitor);
		getActionExecutionListeners().remove(new ActionExecutionMonitor());

		workbench.removeWindowListener(WINDOW_LISTENER);
		for (IWorkbenchWindow w : MonitorUiPlugin.getDefault().getMonitoredWindows()) {
			if (w.getShell() != null) {
				w.getShell().removeShellListener(SHELL_LISTENER);
			}
		}
		logMoveUtility.stop();
		// ContextCore.getPluginPreferences().removePropertyChangeListener(DATA_DIR_MOVE_LISTENER);

		MonitorUiPlugin.getDefault().removeWindowPerspectiveListener(perspectiveMonitor);
		workbench.getActivitySupport().getActivityManager().removeActivityManagerListener(activityMonitor);
		workbench.getDisplay().removeFilter(SWT.Selection, menuMonitor);
		workbench.removeWindowListener(windowMonitor);

		// uninstallBrowserMonitor(workbench);
		interactionLogger.stopMonitoring();

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_STARTED, false);
	}

	public void addMonitoringLifecycleListener(IMonitorLifecycleListener listener) {
		lifecycleListeners.add(listener);
		if (isMonitoringEnabled()) {
			listener.startMonitoring();
		}
	}

	public void removeMonitoringLifecycleListener(IMonitorLifecycleListener listener) {
		lifecycleListeners.remove(listener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	public void actionObserved(IAction action, String info) {
		for (IActionExecutionListener listener : actionExecutionListeners) {
			listener.actionObserved(action);
		}
	}

	public List<IActionExecutionListener> getActionExecutionListeners() {
		return actionExecutionListeners;
	}

	public List<AbstractCommandMonitor> getCommandMonitors() {
		return commandMonitors;
	}

	/**
	 * Parallels TasksUiPlugin.getDefaultDataDirectory()
	 */
	public File getMonitorLogFile() {
		File rootDir = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/.metadata/.mylyn");
		File file = new File(rootDir, MONITOR_LOG_NAME + InteractionContextManager.CONTEXT_FILE_EXTENSION_OLD);
		if (!file.exists() || !file.canWrite()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						"Could not create monitor file", e));
			}
		}
		return file;
	}

	private long getUserPromptDelay() {
		return DELAY_ON_USER_REQUEST / DAY;
	}

	public void userCancelSubmitFeedback(Date currentTime, boolean wait3Hours) {
		if (wait3Hours) {
			lastTransmit.setTime(currentTime.getTime() + DELAY_ON_USER_REQUEST
					- studyParameters.getTransmitPromptPeriod());
			plugin.getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE,
					lastTransmit.getTime());
		} else {
			long day = HOUR * 24;
			lastTransmit.setTime(currentTime.getTime() + day - studyParameters.getTransmitPromptPeriod());
			plugin.getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE,
					lastTransmit.getTime());
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static UiUsageMonitorPlugin getDefault() {
		return plugin;
	}

	// NOTE: not currently used
	synchronized void checkForStatisticsUpload() {
		if (!isMonitoringEnabled()) {
			return;
		}
		if (plugin == null || plugin.getPreferenceStore() == null) {
			return;
		}

		if (plugin.getPreferenceStore().contains(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE)) {

			lastTransmit = new Date(plugin.getPreferenceStore().getLong(
					MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE));
		} else {
			lastTransmit = new Date();
			plugin.getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE,
					lastTransmit.getTime());
		}
		Date currentTime = new Date();

		if (currentTime.getTime() > lastTransmit.getTime() + studyParameters.getTransmitPromptPeriod()
				&& getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION)) {

			String ending = getUserPromptDelay() == 1 ? "" : "s";
			MessageDialog message = new MessageDialog(
					Display.getDefault().getActiveShell(),
					"Send Usage Feedback",
					null,
					"To help improve the Eclipse and Mylyn user experience please consider uploading your UI usage statistics.",
					MessageDialog.QUESTION, new String[] { "Open UI Usage Report",
							"Remind me in " + getUserPromptDelay() + " day" + ending, "Don't ask again" }, 0);
			int result = 0;
			if ((result = message.open()) == 0) {
				// time must be stored right away into preferences, to prevent
				// other threads
				lastTransmit.setTime(new Date().getTime());
				plugin.getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE,
						currentTime.getTime());

				if (!plugin.getPreferenceStore().contains(
						MonitorPreferenceConstants.PREF_MONITORING_MYLYN_ECLIPSE_ORG_CONSENT_VIEWED)
						|| !plugin.getPreferenceStore().getBoolean(
								MonitorPreferenceConstants.PREF_MONITORING_MYLYN_ECLIPSE_ORG_CONSENT_VIEWED)) {
					MessageDialog consentMessage = new MessageDialog(
							Display.getDefault().getActiveShell(),
							"Consent",
							null,
							"All data that is submitted to mylyn.eclipse.org will be publicly available under the "
									+ "Eclipse Public License (EPL).  By submitting your data, you are agreeing that it can be publicly "
									+ "available. Please press cancel on the submission dialog box if you do not wish to share your data.",
							MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0);
					consentMessage.open();
					plugin.getPreferenceStore().setValue(
							MonitorPreferenceConstants.PREF_MONITORING_MYLYN_ECLIPSE_ORG_CONSENT_VIEWED, true);
				}

				NewUsageSummaryEditorWizard wizard = new NewUsageSummaryEditorWizard();
				wizard.init(PlatformUI.getWorkbench(), null);
				// Instantiates the wizard container with the wizard and
				// opens it
				WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
				dialog.create();
				dialog.open();
				/*
				 * the UI usage report is loaded asynchronously so there's no
				 * synchronous way to know if it failed if (wizard.failed()) {
				 * lastTransmit.setTime(currentTime.getTime() + DELAY_ON_FAILURE -
				 * studyParameters.getTransmitPromptPeriod());
				 * plugin.getPreferenceStore().setValue(MylynMonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE,
				 * currentTime.getTime()); }
				 */

			} else {
				if (result == 1) {
					userCancelSubmitFeedback(currentTime, true);
				} else {
					plugin.getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION,
							false);
				}
			}
			message.close();
		}
	}

	public void incrementObservedEvents(int increment) {
		int numEvents = getPreferenceStore().getInt(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS);
		numEvents += increment;
		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS, numEvents);
	}

	public static boolean isPerformingUpload() {
		return performingUpload;
	}

	public static void setPerformingUpload(boolean performingUpload) {
		UiUsageMonitorPlugin.performingUpload = performingUpload;
	}

	public InteractionEventLogger getInteractionLogger() {
		return interactionLogger;
	}

	public StudyParameters getStudyParameters() {
		return studyParameters;
	}

	public boolean isMonitoringEnabled() {
		return getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED);
	}

	private void initializeDefaultStudyParameters() {
		studyParameters = new StudyParameters();
		studyParameters.setVersion("");
		studyParameters.setUploadServletUrl("http://mylyn.eclipse.org/monitor/upload/MylarUsageUploadServlet");
		studyParameters.setUserIdServletUrl("http://mylyn.eclipse.org/monitor/upload/GetUserIDServlet");

		studyParameters.setTitle("Mylyn Feedback");
		studyParameters.setDescription("Fill out the following form to help us improve Mylyn based on your input.\n");
		studyParameters.setTransmitPromptPeriod(DEFAULT_DELAY_BETWEEN_TRANSMITS);
		studyParameters.setUseContactField("false");
		studyParameters.setAcceptedUrlList("");
		studyParameters.setFormsConsent("/doc/study-ethics.html");
		studyParameters.setUsagePageUrl("http://mylyn.eclipse.org/monitor/upload/usageSummary.html");
		studyParameters.setStudyName("Eclipse.org Mylyn");
		studyParameters.addFilteredIdPattern("org.eclipse.");
	}
}
