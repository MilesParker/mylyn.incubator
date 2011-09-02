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

package org.eclipse.mylyn.modeling.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.modeling.ecoretools.EcoreDiagramUiBridge;
import org.eclipse.mylyn.internal.modeling.ecoretools.EcoreGmfDomainBridge;
import org.eclipse.mylyn.modeling.context.AbstractEmfContextTest;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Miles Parker
 */
public class EmfUIBridgeTest extends AbstractEmfContextTest {

	private DiagramUiEditingMonitor monitor;

	@Override
	protected void setUp() throws Exception {
		// ignore
		super.setUp();
		monitor = new DiagramUiEditingMonitor(structureBridge, new EcoreDiagramUiBridge());
		MonitorUi.getSelectionMonitors().add(monitor);
	}

	public void testModification() throws Exception {
		getEmfProject().open(new NullProgressMonitor());
		IProject project = getEmfProject().getProject();
		IFile file = project.getFile("model/library.ecorediag");
		assertNotNull(file);

		assertTrue(file.exists());
		FileEditorInput input = new FileEditorInput(file);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		Thread.sleep(2000);

		EcoreDiagramEditor ed = (EcoreDiagramEditor) page.openEditor(input,
				"org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditorID");

		System.out.println(ContextCore.getContextManager().getActiveContext().getAllElements());

		IInteractionElement element = ContextCore.getContextManager().getElement(
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book");

		assertFalse(element.getInterest().isInteresting());

		EPackage p = (EPackage) ed.getEditingDomain().getResourceSet().getResources().get(0).getContents().get(0);

		EClassifier book = p.getEClassifier("Book");

		monitor.handleWorkbenchPartSelection(ed, new StructuredSelection(book), true);

		Thread.sleep(5000);
		assertNotNull(element);
		assertNotNull(element.getInterest());

		IInteractionElement element2 = ContextCore.getContextManager().getElement(
				"platform:/resource/org.eclipse.mylyn.emf.tests.library/model/library.ecore#//Book");
		assertTrue(element2.getInterest().isInteresting());

		assertEquals(element2.getContentType(), EcoreGmfDomainBridge.ECORE_CONTENT_TYPE);
	}

}
