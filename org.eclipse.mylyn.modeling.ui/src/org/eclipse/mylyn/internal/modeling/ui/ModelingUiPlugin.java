package org.eclipse.mylyn.internal.modeling.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.context.ui.IContextUiStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class ModelingUiPlugin extends AbstractUIPlugin {

	public static final String ID_PLUGIN = "org.eclipse.mylyn.modeling.ui"; //$NON-NLS-1$

	public static final String FOCUSSING_ENABLED = "org.eclipse.mylyn.context.modeling.ui.focus.enabled"; //$NON-NLS-1$

	private static ModelingUiPlugin INSTANCE;

	public ModelingUiPlugin() {
	}

	/**
	 * Startup order is critical.
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		INSTANCE = this;
	}

	private void lazyStart() {
	}

	private void lazyStop() {
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		lazyStop();

		super.stop(context);
		INSTANCE = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ModelingUiPlugin getDefault() {
		return INSTANCE;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(ID_PLUGIN, path);
	}

	public static class ModelingUiStartup implements IContextUiStartup {

		public void lazyStartup() {
			ModelingUiPlugin.getDefault().lazyStart();
		}
	}
}
