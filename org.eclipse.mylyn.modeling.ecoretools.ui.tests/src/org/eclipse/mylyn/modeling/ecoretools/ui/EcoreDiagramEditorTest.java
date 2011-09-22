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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EClassEditPart;
import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.modeling.ecoretools.EcoreDiagramUiBridge;
import org.eclipse.mylyn.internal.modeling.ecoretools.EcoreGmfDomainBridge;
import org.eclipse.mylyn.modeling.context.AbstractEmfContextTest;
import org.eclipse.mylyn.modeling.ui.DiagramUiEditingMonitor;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Miles Parker
 */
public class EcoreDiagramEditorTest extends AbstractEmfContextTest {

	protected DiagramUiEditingMonitor monitor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		structureModelBridge = new EcoreGmfDomainBridge();

		monitor = new DiagramUiEditingMonitor(structureModelBridge, EcoreDiagramUiBridge.getInstance());
		MonitorUi.getSelectionMonitors().add(monitor);
	}

	public void testSelection() throws Exception {

		getEmfProject().open(new NullProgressMonitor());
		IProject project = getEmfProject().getProject();
		IFile file = project.getFile("model/library.ecorediag");
		assertNotNull(file);

		assertTrue(file.exists());
		FileEditorInput input = new FileEditorInput(file);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		EcoreDiagramEditor ed = (EcoreDiagramEditor) page.openEditor(input,
				"org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditorID");

		String elemURI = "platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecorediag#//Diagram";

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		IInteractionContext activeContext = ContextCore.getContextManager().getActiveContext();

		assertNotNull(activeContext);
		assertEquals(activeContext.getAllElements().size(), 1);
		//should this be resource type?
		assertEquals(activeContext.getAllElements().get(0).getContentType(), "ecore");
		assertEquals(activeContext.getAllElements().get(0).getHandleIdentifier(),
				"/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore");
		assertTrue(activeContext.getAllElements().get(0).getInterest().isInteresting());

		IInteractionElement element = ContextCore.getContextManager().getElement(elemURI);
		assertNotNull(element);
		assertFalse(element.getInterest().isInteresting());

		TransactionalEditingDomain domain = ed.getEditingDomain();
		EList<Resource> resources = domain.getResourceSet().getResources();
		EList<EObject> contents = resources.get(0).getContents();
		Node p = (Node) contents.get(0).eContents().get(0);

		EClass book = (EClass) p.getElement();

		assertEquals(book.getName(), "Book");

		String proxyID = EMFCoreUtil.getProxyID(book);
		List<?> findEditPartsForElement = ed.getDiagramGraphicalViewer().findEditPartsForElement(proxyID,
				EClassEditPart.class);

		assertEquals(findEditPartsForElement.size(), 1);
		StructuredSelection selection = new StructuredSelection(findEditPartsForElement);
		monitor.handleWorkbenchPartSelection(ed, selection, true);

		EditPart editpart = (EditPart) findEditPartsForElement.get(0);
		ed.getDiagramGraphicalViewer().getSelectionManager().appendSelection(editpart);
		ed.getDiagramGraphicalViewer().getRootEditPart().refresh();
		assertTrue(ed.getDiagramGraphicalViewer().getSelectedEditParts().get(0) == editpart);

		assertEquals(activeContext.getAllElements().size(), 2);
		assertTrue(checkInterest(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book", "ecore")); //$NON-NLS-1$

		Command changeName = SetCommand.create(domain, book, EcorePackage.Literals.ENAMED_ELEMENT__NAME, "Livre"); //$NON-NLS-1$
		domain.getCommandStack().execute(changeName);

		assertEquals(activeContext.getAllElements().size(), 2);
		assertEquals(book.getName(), "Livre"); //$NON-NLS-1$
		assertTrue(checkInterest(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Livre", "ecore")); //$NON-NLS-1$
		assertFalse(checkInterest(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book", "ecore")); //$NON-NLS-1$

		domain.getCommandStack().undo();

		assertEquals(activeContext.getAllElements().size(), 2);
		assertFalse(checkInterest(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Livre", "ecore")); //$NON-NLS-1$
		assertTrue(checkInterest(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book", "ecore")); //$NON-NLS-1$
	}

	private boolean checkInterest(IInteractionContext activeContext, String id, String type) {
		boolean found = false;
		for (IInteractionElement elem : activeContext.getAllElements()) {
			assertEquals(elem.getContentType(), type);
			if (elem.getHandleIdentifier().equals(id)) {
				found = true;
			}
			assertTrue(elem.getInterest().isInteresting());
		}
		return found;
	}
}
