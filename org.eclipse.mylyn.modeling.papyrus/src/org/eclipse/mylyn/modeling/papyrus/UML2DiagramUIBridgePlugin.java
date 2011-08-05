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

package org.eclipse.mylyn.modeling.papyrus;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.ui.IContextUiStartup;
import org.eclipse.mylyn.modeling.context.EMFStructureBridge;
import org.eclipse.mylyn.modeling.ui.DiagramUIEditingMonitor;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author mparker
 */
public class UML2DiagramUIBridgePlugin extends AbstractUIPlugin {

	public static final String ID_PLUGIN = "org.eclipse.mylyn.modeling.ecoretools"; //$NON-NLS-1$

	private static UML2DiagramUIBridgePlugin INSTANCE;

	private DiagramUIEditingMonitor monitor;


	public UML2DiagramUIBridgePlugin() {
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
		AbstractContextStructureBridge structureBridge = ContextCore.getStructureBridge(UML2DomainBridge.UML2_CONTENT_TYPE);
		if (structureBridge instanceof EMFStructureBridge) {
			structureBridge = new UML2StructureBridge();
		}
		EMFStructureBridge bridge = (EMFStructureBridge) structureBridge;
		monitor = new DiagramUIEditingMonitor(bridge, UML2DomainBridge.getInstance());
		MonitorUi.getSelectionMonitors().add(monitor);
	}

	private void lazyStop() {
		MonitorUi.getSelectionMonitors().remove(monitor);
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
	public static UML2DiagramUIBridgePlugin getDefault() {
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

	public static class UML2DiagramBridgeStartup implements IContextUiStartup {

		public void lazyStartup() {
			UML2DiagramUIBridgePlugin.getDefault().lazyStart();
		}

	}

}
