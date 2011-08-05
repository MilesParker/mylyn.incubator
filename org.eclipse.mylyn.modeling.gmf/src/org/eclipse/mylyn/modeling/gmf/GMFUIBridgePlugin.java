package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.mylyn.context.ui.IContextUiStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GMFUIBridgePlugin extends AbstractUIPlugin {


	public static class GMFUiBridgeStartup implements IContextUiStartup {

		public void lazyStartup() {
			GMFUIBridgePlugin.getDefault().lazyStart();
		}

	}
	
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.mylyn.gmf.ui"; //$NON-NLS-1$

	private static GMFUIBridgePlugin INSTANCE;
	
	/**
	 * The constructor
	 */
	public GMFUIBridgePlugin() {
	}

	private void lazyStart() {
	}

	private void lazyStop() {
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		INSTANCE = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		lazyStop();

		super.stop(context);
		INSTANCE = null;
	}
	
	public static GMFUIBridgePlugin getDefault() {
		return INSTANCE;
	}
}
