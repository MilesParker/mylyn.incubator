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

package org.eclipse.mylyn.modeling.papyrus.ui;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.modeling.papyrus.Uml2StructureBridge;
import org.eclipse.mylyn.internal.modeling.papyrus.Uml2UiBridge;
import org.eclipse.mylyn.modeling.context.AbstractEmfContextTest;
import org.eclipse.mylyn.modeling.context.DomainModelContextStructureBridge;
import org.eclipse.mylyn.modeling.tests.WorkspaceSetupHelper;
import org.eclipse.mylyn.modeling.ui.DiagramUiEditingMonitor;
import org.eclipse.papyrus.diagram.common.editparts.IPapyrusEditPart;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;

/**
 * @author Miles Parker
 */
public class PapyrusDiagramEditorTest extends AbstractEmfContextTest {

	private static final String RESOURCE_URI = "platform:/resource/org.eclipse.mylyn.emf.tests.papyrus/model/model.uml#_xkh2ALJFEeCYupgj-BJj-Q";

	protected DomainModelContextStructureBridge structureBridge;

	private IJavaProject papyrusProject;

	private DiagramUiEditingMonitor monitor;

	@Override
	protected void setUp() throws Exception {
		// ignore
		super.setUp();
		structureBridge = new Uml2StructureBridge();
		monitor = new DiagramUiEditingMonitor(structureBridge, Uml2UiBridge.getInstance());
		papyrusProject = WorkspaceSetupHelper.createJavaPluginProjectFromZip(
				"org.eclipse.mylyn.modeling.tests.papyrus", "papyrus.zip");
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

		PapyrusMultiDiagramEditor ed = (PapyrusMultiDiagramEditor) page.openEditor(input,
				"org.eclipse.papyrus.core.papyrusEditor");

		System.out.println(ContextCore.getContextManager().getActiveContext().getAllElements());

		String elemURI = RESOURCE_URI;
		IInteractionElement element = ContextCore.getContextManager().getElement(elemURI);
		assertNotNull(element);
		assertFalse(element.getInterest().isInteresting());
		IInteractionElement iInteractionElement = ContextCore.getContextManager().getActiveContext().get(RESOURCE_URI);
		assertFalse(iInteractionElement.getInterest().isInteresting());

		ClassImpl book = (ClassImpl) ed.getEditingDomain()
				.getResourceSet()
				.getResources()
				.get(0)
				.getContents()
				.get(0)
				.eContents()
				.get(0);

		assertEquals(book.getName(), "Book");

		List<?> findEditPartsForElement = ed.getDiagramGraphicalViewer().findEditPartsForElement(
				EMFCoreUtil.getProxyID(book), IPapyrusEditPart.class);

		assertEquals(findEditPartsForElement.size(), 1);
		StructuredSelection selection = new StructuredSelection(findEditPartsForElement);
		monitor.handleWorkbenchPartSelection(ed, selection, true);

		Thread.sleep(5000);
		//TODO why doesn't this work?
//		ed.getDiagramGraphicalViewer().setSelection(selection);
//		ed.getDiagramGraphicalViewer().getRootEditPart().refresh();
//		assertTrue(ed.getDiagramGraphicalViewer().getSelectedEditParts().get(0) instanceof EClassEditPart);

		assertNotNull(element);
		assertNotNull(element.getInterest());

		assertNotNull(iInteractionElement);

		IInteractionElement element2 = ContextCore.getContextManager().getElement(RESOURCE_URI);
		iInteractionElement = ContextCore.getContextManager().getActiveContext().get(RESOURCE_URI);

		//TODO why doesn't this work? Can we fix?
//		assertTrue(iInteractionElement.getInterest().isInteresting());
//		assertTrue(element2.getInterest().isInteresting());

//		assertEquals(element2.getContentType(), UML2DomainBridge.UML2_CONTENT_TYPE);
	}

	@Override
	protected void tearDown() throws Exception {
//		super.tearDown();
//		ResourceTestUtil.deleteProject(papyrusProject.getProject());
	}

}
