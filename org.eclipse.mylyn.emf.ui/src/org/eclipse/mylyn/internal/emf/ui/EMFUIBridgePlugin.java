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

import org.eclipse.core.runtime.Plugin;
import org.eclipse.mylyn.context.ui.IContextUiStartup;
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

	private EMFUIEditingMonitor emfEditingMonitor;

	public EMFUIBridgePlugin() {
	}

	private void lazyStart() {
		emfEditingMonitor = new EMFUIEditingMonitor();
		MonitorUi.getSelectionMonitors().add(emfEditingMonitor);
	}

	private void lazyStop() {
		if (emfEditingMonitor != null) {
			MonitorUi.getSelectionMonitors().remove(emfEditingMonitor);
		}
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
