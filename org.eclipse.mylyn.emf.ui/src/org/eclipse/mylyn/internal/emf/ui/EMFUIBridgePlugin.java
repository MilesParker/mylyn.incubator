/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.emf.ui;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.ui.AbstractContextUiBridge;
import org.eclipse.mylyn.context.ui.IContextUiStartup;
import org.eclipse.mylyn.emf.context.EMFStructureBridge;
import org.eclipse.mylyn.emf.context.IDiagramContextBridge;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.ui.ContextUiPlugin;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.osgi.framework.BundleContext;

/**
 * @author Benjamin Muskalla
 */
public class EMFUIBridgePlugin extends Plugin {

	public static class EMFUIBridgeStartup implements IContextUiStartup {

		public void lazyStartup() {
			EMFUIBridgePlugin.getDefault().lazyStart();
		}

	}

	public static final String ID_PLUGIN = "org.eclipse.mylyn.emf.ui"; //$NON-NLS-1$

	private static EMFUIBridgePlugin INSTANCE;

	public static EMFUIBridgePlugin getDefault() {
		return INSTANCE;
	}

	private final Collection<AbstractUserInteractionMonitor> monitors = new ArrayList<AbstractUserInteractionMonitor>();

	public EMFUIBridgePlugin() {
	}

	private static final String EXTENSION_ID_DIAGRAM = "org.eclipse.mylyn.emf.context.diagram";

	private static final String ELEMENT_DIAGRAM_BRIDGE = "diagramBridge";

	private static final String ATTR_CLASS = "class";

	private static final String ATTR_CONTENT_TYPE = "contentType";

	boolean initialized;

	public void initExtensions() {
		if (!initialized) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();

			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_DIAGRAM);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals(ELEMENT_DIAGRAM_BRIDGE)) {
						try {
							Object object = element.createExecutableExtension(ATTR_CLASS);
							if (!(object instanceof IDiagramContextBridge)) {
								StatusHandler.log(new Status(IStatus.ERROR, ID_PLUGIN, "Could not load bridge: "
										+ object.getClass().getCanonicalName() + " must implement "
										+ IDiagramContextBridge.class.getCanonicalName()));
								return;
							}
							EMFStructureBridge delegatingBridge = new EMFStructureBridge((IDiagramContextBridge) object);
							ContextCorePlugin.getDefault().addStructureBridge(delegatingBridge);
							AbstractContextUiBridge uiBridge = ContextUiPlugin.getDefault().getUiBridge(
									delegatingBridge.getContentType());
//							if (!(uiBridge instanceof GenericUIBridge)) {
//								StatusHandler.log(new Status(IStatus.ERROR, ID_PLUGIN, "Could not locate UI bridge: "
//										+ uiBridge.getClass().getCanonicalName() + " must implement "
//										+ GenericUIBridge.class.getCanonicalName()));
//								return;
//							}
							DiagramUIEditingMonitor monitor = new DiagramUIEditingMonitor(delegatingBridge, uiBridge);
							monitors.add(monitor);
						} catch (Throwable e) {
							StatusHandler.log(new Status(IStatus.WARNING, ID_PLUGIN, "Could not load extension", e));
						}
					}
				}
			}
			MonitorUi.getSelectionMonitors().addAll(monitors);
			initialized = true;
		}
	}

	private void lazyStart() {
		initExtensions();
	}

	private void lazyStop() {
		MonitorUi.getSelectionMonitors().removeAll(monitors);
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

}
