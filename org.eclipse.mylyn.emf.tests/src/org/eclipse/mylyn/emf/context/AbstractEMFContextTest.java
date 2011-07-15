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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.mylyn.context.tests.AbstractContextTest;
import org.eclipse.mylyn.emf.tests.WorkspaceSetupHelper;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.core.InteractionContext;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.internal.context.core.InteractionContextScaling;
import org.eclipse.mylyn.internal.context.ui.ContextUiPlugin;
import org.eclipse.mylyn.internal.emf.ui.EMFUIEditingMonitor;
import org.eclipse.mylyn.internal.ide.ui.IdeUiBridgePlugin;
import org.eclipse.mylyn.resources.tests.ResourceTestUtil;

public class AbstractEMFContextTest extends AbstractContextTest {

	protected InteractionContextManager manager = ContextCorePlugin.getContextManager();

	protected EMFUIEditingMonitor monitor = new EMFUIEditingMonitor();

	protected EMFStructureBridge structureBridge = new EMFStructureBridge();

	protected InteractionContext context;

	protected InteractionContextScaling scaling = new InteractionContextScaling();

	protected String taskId = this.getClass().getName();

	IJavaProject emfProject;

	@Override
	protected void setUp() throws Exception {
		emfProject = WorkspaceSetupHelper.getEMFLibraryProject();
		assertNotNull(IdeUiBridgePlugin.getDefault());
		context = new InteractionContext(taskId, scaling);
		context.reset();
		manager.internalActivateContext(context);
		ContextUiPlugin.getViewerManager().setSyncRefreshMode(true);
	}

	@Override
	protected void tearDown() throws Exception {
		context.reset();
		assertTrue(context.getInteresting().isEmpty());
		manager.deactivateContext(taskId);
		manager.deleteContext(taskId);
		ContextCorePlugin.getContextStore().getFileForContext(taskId).delete();
		ResourceTestUtil.deleteProject(emfProject.getProject());
		super.tearDown();
	}

	public IJavaProject getEmfProject() {
		return emfProject;
	}
}
