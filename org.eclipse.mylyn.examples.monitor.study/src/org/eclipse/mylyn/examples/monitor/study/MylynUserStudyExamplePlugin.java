/*******************************************************************************
 * Copyright (c) 2005, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.mylyn.examples.monitor.study;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.monitor.ui.MonitorUiPlugin;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 */
public class MylynUserStudyExamplePlugin extends AbstractUIPlugin implements IStartup {

	private static MylynUserStudyExamplePlugin plugin;

	private SelectionMonitor selectionMonitor;

	public MylynUserStudyExamplePlugin() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void earlyStartup() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				selectionMonitor = new SelectionMonitor();
				MonitorUiPlugin.getDefault().getSelectionMonitors().add(selectionMonitor);

				UiUsageMonitorPlugin.getDefault().addMonitoredPreferences(
						WorkbenchPlugin.getDefault().getPluginPreferences());
				// MylarUsageMonitorPlugin.getDefault().addMonitoredPreferences(
				// MylarUiPlugin.getDefault().getPluginPreferences());
				UiUsageMonitorPlugin.getDefault().addMonitoredPreferences(
						JavaPlugin.getDefault().getPluginPreferences());
				UiUsageMonitorPlugin.getDefault().addMonitoredPreferences(
						WorkbenchPlugin.getDefault().getPluginPreferences());
				UiUsageMonitorPlugin.getDefault().addMonitoredPreferences(
						EditorsPlugin.getDefault().getPluginPreferences());
				UiUsageMonitorPlugin.getDefault()
						.addMonitoredPreferences(PDEPlugin.getDefault().getPluginPreferences());
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;

		MonitorUiPlugin.getDefault().getSelectionMonitors().remove(selectionMonitor);
		UiUsageMonitorPlugin.getDefault().removeMonitoredPreferences(
				WorkbenchPlugin.getDefault().getPluginPreferences());
		UiUsageMonitorPlugin.getDefault().removeMonitoredPreferences(
				ContextCorePlugin.getDefault().getPluginPreferences());
		UiUsageMonitorPlugin.getDefault().removeMonitoredPreferences(JavaPlugin.getDefault().getPluginPreferences());
		UiUsageMonitorPlugin.getDefault().removeMonitoredPreferences(
				WorkbenchPlugin.getDefault().getPluginPreferences());
		UiUsageMonitorPlugin.getDefault().removeMonitoredPreferences(EditorsPlugin.getDefault().getPluginPreferences());
		UiUsageMonitorPlugin.getDefault().removeMonitoredPreferences(PDEPlugin.getDefault().getPluginPreferences());
	}

	/**
	 * Returns the shared instance.
	 */
	public static MylynUserStudyExamplePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylyn.examples.monitor.study", path);
	}
}
