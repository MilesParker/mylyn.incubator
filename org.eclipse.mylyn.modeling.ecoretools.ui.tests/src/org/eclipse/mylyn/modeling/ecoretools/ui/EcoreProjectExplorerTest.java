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

package org.eclipse.mylyn.modeling.ecoretools.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.modeling.ecoretools.EcoreGmfDomainBridge;
import org.eclipse.mylyn.internal.modeling.ecoretools.EcoreToolsNavigatorUiBridge;
import org.eclipse.mylyn.modeling.context.AbstractEmfContextTest;
import org.eclipse.mylyn.modeling.ui.DiagramUiEditingMonitor;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

/**
 * @author Miles Parker
 */
public class EcoreProjectExplorerTest extends AbstractEmfContextTest {

	protected DiagramUiEditingMonitor monitor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		structureModelBridge = new EcoreGmfDomainBridge();

		monitor = new DiagramUiEditingMonitor(structureModelBridge, EcoreToolsNavigatorUiBridge.getInstance());
		MonitorUi.getSelectionMonitors().add(monitor);
	}

	public void testSelection() throws Exception {

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		ProjectExplorer pe = (ProjectExplorer) page.showView("org.eclipse.ui.navigator.ProjectExplorer");

		getEmfProject().open(new NullProgressMonitor());
		IProject project = getEmfProject().getProject();
		IFile file = project.getFile("model/library.ecorediag");
		assertNotNull(file);

		assertTrue(file.exists());

		String elemURI = "platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecorediag#//Diagram";

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		IInteractionContext activeContext = ContextCore.getContextManager().getActiveContext();

		//TODO..we probably need SWTBot to really excercise this well..sigh.
//		StructuredSelection selection = new StructuredSelection(file);
//		pe.getCommonViewer().setSelection(selection);
//		monitor.handleWorkbenchPartSelection(pe, selection, true);
//
//		assertNotNull(activeContext);
//		assertEquals(activeContext.getAllElements().size(), 1);
//		//should this be resource type?
//		assertEquals(activeContext.getAllElements().get(0).getContentType(), "ecore");
//		assertEquals(activeContext.getAllElements().get(0).getHandleIdentifier(),
//				"/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore");
//		assertTrue(activeContext.getAllElements().get(0).getInterest().isInteresting());
	}

	private boolean checkInterest(IInteractionContext activeContext, String id) {
		boolean found = false;
		for (IInteractionElement elem : activeContext.getAllElements()) {
			assertEquals(elem.getContentType(), "ecore"); //$NON-NLS-1$
			if (elem.getHandleIdentifier().equals(id)) {
				found = true;
			}
			assertTrue(elem.getInterest().isInteresting());
		}
		return found;
	}
}
