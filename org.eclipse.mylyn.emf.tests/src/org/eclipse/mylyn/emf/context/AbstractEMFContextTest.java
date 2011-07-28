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

package org.eclipse.mylyn.emf.context;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.mylyn.emf.tests.WorkspaceSetupHelper;
import org.eclipse.mylyn.internal.emf.ui.EMFUIBridgePlugin;
import org.eclipse.mylyn.resources.tests.ResourceTestUtil;

public class AbstractEMFContextTest extends AbstractDiagramContextTest {

	protected DomainAdaptedStructureBridge structureBridge;

	IJavaProject emfProject;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EMFUIBridgePlugin.getDefault().initExtensions();
		structureBridge = new EMFStructureBridge(new EcoreDiagramBridge());
		emfProject = WorkspaceSetupHelper.createJavaPluginProjectFromZip("org.eclipse.mylyn.emf.tests.library",
				"library.zip");
		emfProject.open(new NullProgressMonitor());
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceTestUtil.deleteProject(emfProject.getProject());
		super.tearDown();
	}

	public IJavaProject getEmfProject() {
		return emfProject;
	}
}
