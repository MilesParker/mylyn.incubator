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

package org.eclipse.mylyn.modeling.ecoretools;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.ui.IContextUiStartup;
import org.eclipse.mylyn.modeling.context.EMFStructureBridge;
import org.eclipse.mylyn.modeling.ui.DiagramUIEditingMonitor;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author mparker
 */
public class EcoreDiagramUIBridgePlugin extends AbstractUIPlugin {

	public static final String ID_PLUGIN = "org.eclipse.mylyn.modeling.ecoretools"; //$NON-NLS-1$

	private static EcoreDiagramUIBridgePlugin INSTANCE;

	private DiagramUIEditingMonitor diagramMonitor;

	private AbstractUserInteractionMonitor navigatorMonitor;

	public EcoreDiagramUIBridgePlugin() {
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
		AbstractContextStructureBridge structureBridge = ContextCore
				.getStructureBridge(EcoreDiagramDomainBridge.ECORE_CONTENT_TYPE);
		// we'll get resource by default -- shouldn't we get null as failure
		// case? https://bugs.eclipse.org/bugs/show_bug.cgi?id=353439
		if (structureBridge instanceof EMFStructureBridge) {
			EMFStructureBridge bridge = (EMFStructureBridge) structureBridge;
			diagramMonitor = new DiagramUIEditingMonitor(bridge,
					EcoreDiagramDomainBridge.getInstance());
			MonitorUi.getSelectionMonitors().add(diagramMonitor);
			navigatorMonitor = new EcoreToolsUIInteractionMonitor(bridge);
			MonitorUi.getSelectionMonitors().add(navigatorMonitor);
		} else {
			StatusHandler
					.log(new Status(
							IStatus.WARNING,
							ID_PLUGIN,
							"Couldn't load EMFStructure Bridge for " + EcoreDiagramDomainBridge.ECORE_CONTENT_TYPE)); //$NON-NLS-1$	
		}
	}

	private void lazyStop() {
		if (diagramMonitor != null) {
			MonitorUi.getSelectionMonitors().remove(diagramMonitor);
		}
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
	public static EcoreDiagramUIBridgePlugin getDefault() {
		return INSTANCE;
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
		return AbstractUIPlugin.imageDescriptorFromPlugin(ID_PLUGIN, path);
	}

	public static class EcoreDiagramBridgeStartup implements IContextUiStartup {

		public void lazyStartup() {
			EcoreDiagramUIBridgePlugin.getDefault().lazyStart();
		}

	}

}
