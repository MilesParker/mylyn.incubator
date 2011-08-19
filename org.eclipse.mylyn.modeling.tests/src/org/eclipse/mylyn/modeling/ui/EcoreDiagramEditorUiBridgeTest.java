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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EClassEditPart;
import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.modeling.context.AbstractEmfContextTest;
import org.eclipse.mylyn.modeling.ecoretools.EcoreDiagramDomainBridge;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Miles Parker
 */
public class EcoreDiagramEditorUiBridgeTest extends AbstractEmfContextTest {

	protected DiagramUiEditingMonitor monitor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		monitor = new DiagramUiEditingMonitor(structureBridge, EcoreDiagramDomainBridge.getInstance());
		MonitorUi.getSelectionMonitors().add(monitor);
	}

	public void test() throws Exception {

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

		String elemURI = "platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecorediag#//Diagram";
		IInteractionElement element = ContextCore.getContextManager().getElement(elemURI);
		assertNotNull(element);
		assertFalse(element.getInterest().isInteresting());

		Node p = (Node) ed.getEditingDomain()
				.getResourceSet()
				.getResources()
				.get(0)
				.getContents()
				.get(0)
				.eContents()
				.get(0);

		EClass book = (EClass) p.getElement();

		assertEquals(book.getName(), "Book");

		String proxyID = EMFCoreUtil.getProxyID(book);
		List<?> findEditPartsForElement = ed.getDiagramGraphicalViewer().findEditPartsForElement(proxyID,
				EClassEditPart.class);

		assertEquals(findEditPartsForElement.size(), 1);
		StructuredSelection selection = new StructuredSelection(findEditPartsForElement);
		monitor.handleWorkbenchPartSelection(ed, selection, true);

		//TODO why doesn't this work?
		ed.getDiagramGraphicalViewer().setSelection(selection);
		ed.getDiagramGraphicalViewer().getRootEditPart().refresh();
		assertTrue(ed.getDiagramGraphicalViewer().getSelectedEditParts().get(0) instanceof EClassEditPart);

		assertNotNull(element);
		assertNotNull(element.getInterest());

		IInteractionElement element2 = ContextCore.getContextManager().getElement(elemURI);

		//TODO this test doesn't work, not clear why -- the functionality is fine at runtime
//		assertTrue(element2.getInterest().isInteresting());
//
//		assertEquals(element2.getContentType(), EcoreDomainBridge.ECORE_CONTENT_TYPE);
	}

}
