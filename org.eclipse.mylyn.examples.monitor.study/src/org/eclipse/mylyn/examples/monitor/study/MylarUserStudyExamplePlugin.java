package org.eclipse.mylar.examples.monitor.study;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.internal.monitor.usage.MylarUsageMonitorPlugin;
import org.eclipse.mylar.monitor.ui.MonitorUiPlugin;
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
public class MylarUserStudyExamplePlugin extends AbstractUIPlugin implements IStartup {

	private static MylarUserStudyExamplePlugin plugin;

	private SelectionMonitor selectionMonitor;

	public MylarUserStudyExamplePlugin() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void earlyStartup() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				selectionMonitor = new SelectionMonitor();
				MonitorUiPlugin.getDefault().getSelectionMonitors().add(selectionMonitor);

				MylarUsageMonitorPlugin.getDefault().addMonitoredPreferences(
						WorkbenchPlugin.getDefault().getPluginPreferences());
				// MylarUsageMonitorPlugin.getDefault().addMonitoredPreferences(
				// MylarUiPlugin.getDefault().getPluginPreferences());
				MylarUsageMonitorPlugin.getDefault().addMonitoredPreferences(
						JavaPlugin.getDefault().getPluginPreferences());
				MylarUsageMonitorPlugin.getDefault().addMonitoredPreferences(
						WorkbenchPlugin.getDefault().getPluginPreferences());
				MylarUsageMonitorPlugin.getDefault().addMonitoredPreferences(
						EditorsPlugin.getDefault().getPluginPreferences());
				MylarUsageMonitorPlugin.getDefault().addMonitoredPreferences(
						PDEPlugin.getDefault().getPluginPreferences());
			}
		});
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;

		MonitorUiPlugin.getDefault().getSelectionMonitors().remove(selectionMonitor);
		MylarUsageMonitorPlugin.getDefault().removeMonitoredPreferences(
				WorkbenchPlugin.getDefault().getPluginPreferences());
		MylarUsageMonitorPlugin.getDefault().removeMonitoredPreferences(
				ContextCorePlugin.getDefault().getPluginPreferences());
		MylarUsageMonitorPlugin.getDefault().removeMonitoredPreferences(JavaPlugin.getDefault().getPluginPreferences());
		MylarUsageMonitorPlugin.getDefault().removeMonitoredPreferences(
				WorkbenchPlugin.getDefault().getPluginPreferences());
		MylarUsageMonitorPlugin.getDefault().removeMonitoredPreferences(
				EditorsPlugin.getDefault().getPluginPreferences());
		MylarUsageMonitorPlugin.getDefault().removeMonitoredPreferences(PDEPlugin.getDefault().getPluginPreferences());
	}

	/**
	 * Returns the shared instance.
	 */
	public static MylarUserStudyExamplePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylar.examples.monitor.study", path);
	}
}
