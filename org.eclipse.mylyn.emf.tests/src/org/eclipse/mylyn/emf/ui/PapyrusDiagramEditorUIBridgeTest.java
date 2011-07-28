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

package org.eclipse.mylyn.emf.ui;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.diagram.papyrus.UML2DiagramBridge;
import org.eclipse.mylyn.diagram.papyrus.UML2UIBridge;
import org.eclipse.mylyn.emf.context.AbstractDiagramContextTest;
import org.eclipse.mylyn.emf.context.DomainAdaptedStructureBridge;
import org.eclipse.mylyn.emf.context.EMFStructureBridge;
import org.eclipse.mylyn.emf.tests.WorkspaceSetupHelper;
import org.eclipse.mylyn.internal.emf.ui.DiagramUIEditingMonitor;
import org.eclipse.mylyn.resources.tests.ResourceTestUtil;
import org.eclipse.papyrus.diagram.common.editparts.IPapyrusEditPart;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;

public class PapyrusDiagramEditorUIBridgeTest extends AbstractDiagramContextTest {

	protected DomainAdaptedStructureBridge structureBridge;

	private IJavaProject papyrusProject;

	private DiagramUIEditingMonitor monitor;

	@Override
	protected void setUp() throws Exception {
		// ignore
		super.setUp();
		structureBridge = new EMFStructureBridge(new UML2DiagramBridge());
		monitor = new DiagramUIEditingMonitor(structureBridge, new UML2UIBridge());
		papyrusProject = WorkspaceSetupHelper.createJavaPluginProjectFromZip("org.eclipse.mylyn.emf.tests.papyrus",
				"papyrus.zip");
		papyrusProject.open(new NullProgressMonitor());
	}

	public void test() throws Exception {

		papyrusProject.open(new NullProgressMonitor());
		IProject project = papyrusProject.getProject();
		IFile file = project.getFile("model/model.di");
		assertNotNull(file);

		assertTrue(file.exists());
		FileEditorInput input = new FileEditorInput(file);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		Thread.sleep(2000);

		PapyrusMultiDiagramEditor ed = (PapyrusMultiDiagramEditor) page.openEditor(input,
				"org.eclipse.papyrus.core.papyrusEditor");

		System.out.println(ContextCore.getContextManager().getActiveContext().getAllElements());

		String elemURI = "platform:/resource/org.eclipse.mylyn.emf.tests.papyrus/model/model.di#//_xkh2ALJFEeCYupgj-BJj-Q";
		IInteractionElement element = ContextCore.getContextManager().getElement(elemURI);
		assertNotNull(element);
		assertFalse(element.getInterest().isInteresting());

		ClassImpl book = (ClassImpl) ed.getEditingDomain()
				.getResourceSet()
				.getResources()
				.get(0)
				.getContents()
				.get(0)
				.eContents()
				.get(0);

		assertEquals(book.getName(), "Book");

		List findEditPartsForElement = ed.getDiagramGraphicalViewer().findEditPartsForElement(
				EMFCoreUtil.getProxyID(book), IPapyrusEditPart.class);

		assertEquals(findEditPartsForElement.size(), 1);
		StructuredSelection selection = new StructuredSelection(findEditPartsForElement);
		monitor.handleWorkbenchPartSelection(ed, selection, true);

		//TODO why doesn't this work?
//		ed.getDiagramGraphicalViewer().setSelection(selection);
//		ed.getDiagramGraphicalViewer().getRootEditPart().refresh();
//		assertTrue(ed.getDiagramGraphicalViewer().getSelectedEditParts().get(0) instanceof EClassEditPart);

		assertNotNull(element);
		assertNotNull(element.getInterest());

		System.err.println(ContextCore.getContextManager().getActiveContext().getAllElements());

		IInteractionElement element2 = ContextCore.getContextManager().getElement(
				"platform:/resource/org.eclipse.mylyn.emf.tests.papyrus/model/model.uml#_xkh2ALJFEeCYupgj-BJj-Q");

		assertTrue(element2.getInterest().isInteresting());

		assertEquals(element2.getContentType(), UML2DiagramBridge.UML2_CONTENT_TYPE);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ResourceTestUtil.deleteProject(papyrusProject.getProject());
	}

}
