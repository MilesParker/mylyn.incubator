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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EClass2EditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EClassEditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EPackage2EditPart;
import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
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

	private IInteractionContext activeContext;

	private EcoreDiagramEditor editor;

	private Resource diagramResource;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		structureModelBridge = new EcoreGmfDomainBridge();

		monitor = new DiagramUiEditingMonitor(structureModelBridge, EcoreDiagramUiBridge.getInstance());
		MonitorUi.getSelectionMonitors().add(monitor);
	}

	@SuppressWarnings("nls")
	public void testSelection() throws Exception {

		getEmfProject().open(new NullProgressMonitor());
		IProject project = getEmfProject().getProject();
		IFile file = project.getFile("model/library.ecorediag");
		assertNotNull(file);

		assertTrue(file.exists());
		FileEditorInput input = new FileEditorInput(file);

		activeContext = ContextCore.getContextManager().getActiveContext();
		assertNotNull(activeContext);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		editor = (EcoreDiagramEditor) page.openEditor(input,
				"org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditorID");
		TransactionalEditingDomain domain = editor.getEditingDomain();
		EList<Resource> resources = domain.getResourceSet().getResources();
		diagramResource = resources.get(0);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		subTestDiagramOpen(activeContext);

		subTestSelectDiagramElement();

		subtestChangeName(domain);

		subtestRemoveContext();

		subtestSubpackage();

	}

	@SuppressWarnings("nls")
	private void subtestRemoveContext() {
		IInteractionElement elem = activeContext.get("platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book");
		ContextCorePlugin.getContextManager().manipulateInterestForElements(Collections.singletonList(elem), false,
				false, false, "modeling.test", activeContext, true);
		assertEquals(activeContext.getAllElements().size(), 7);
		ContextCorePlugin.getContextManager().manipulateInterestForElements(Collections.singletonList(elem), true,
				false, false, "modeling.test", activeContext, true);
		assertEquals(activeContext.getAllElements().size(), 8);
	}

	@SuppressWarnings("nls")
	private void subtestSubpackage() {
		EPackage root = (EPackage) ((Diagram) diagramResource.getContents().get(0)).getElement();
		EPackage pack = root.getESubpackages().get(0);
		assertEquals(pack.getName(), "Lending");
		String proxyID = EMFCoreUtil.getProxyID(pack);
		List<?> findEditPartsForElement = editor.getDiagramGraphicalViewer().findEditPartsForElement(proxyID,
				EPackage2EditPart.class);
		assertEquals(findEditPartsForElement.size(), 1);
		StructuredSelection selection = new StructuredSelection(findEditPartsForElement);
		monitor.handleWorkbenchPartSelection(editor, selection, true);
		assertEquals(activeContext.getAllElements().size(), 9);
		assertExists(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Lending",
				"ecore");

		IInteractionElement elem = activeContext.get("platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Lending");
		ContextCorePlugin.getContextManager().manipulateInterestForElements(Collections.singletonList(elem), false,
				false, false, "modeling.test", activeContext, true);
		assertEquals(activeContext.getAllElements().size(), 8);
		ContextCorePlugin.getContextManager().manipulateInterestForElements(Collections.singletonList(elem), true,
				false, false, "modeling.test", activeContext, true);
		assertEquals(activeContext.getAllElements().size(), 9);

		EClass checkout = (EClass) pack.getEClassifiers().get(0);
		assertEquals(checkout.getName(), "CheckoutActivity");
		proxyID = EMFCoreUtil.getProxyID(checkout);
		findEditPartsForElement = editor.getDiagramGraphicalViewer().findEditPartsForElement(proxyID,
				EClass2EditPart.class);
		assertEquals(findEditPartsForElement.size(), 1);
		selection = new StructuredSelection(findEditPartsForElement);
		monitor.handleWorkbenchPartSelection(editor, selection, true);
		assertEquals(activeContext.getAllElements().size(), 10);
		assertExists(
				activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Lending/CheckoutActivity",
				"ecore");

		elem = activeContext.get("platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Lending/CheckoutActivity");
		ContextCorePlugin.getContextManager().manipulateInterestForElements(Collections.singletonList(elem), false,
				false, false, "modeling.test", activeContext, true);
		assertEquals(activeContext.getAllElements().size(), 9);
		assertNotExists(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Lending/CheckoutActivity");
		ContextCorePlugin.getContextManager().manipulateInterestForElements(Collections.singletonList(elem), true,
				false, false, "modeling.test", activeContext, true);
		assertEquals(activeContext.getAllElements().size(), 10);

		elem = activeContext.get("platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Lending");
		ContextCorePlugin.getContextManager().manipulateInterestForElements(Collections.singletonList(elem), false,
				false, false, "modeling.test", activeContext, true);
		assertEquals(activeContext.getAllElements().size(), 9);
		ContextCorePlugin.getContextManager().manipulateInterestForElements(Collections.singletonList(elem), true,
				false, false, "modeling.test", activeContext, true);
		assertEquals(activeContext.getAllElements().size(), 10);
	}

	private void subTestSelectDiagramElement() {
		EClass book = (EClass) ((Node) diagramResource.getContents().get(0).eContents().get(0)).getElement();
		assertEquals(book.getName(), "Book");
		String proxyID = EMFCoreUtil.getProxyID(book);
		List<?> findEditPartsForElement = editor.getDiagramGraphicalViewer().findEditPartsForElement(proxyID,
				EClassEditPart.class);
		assertEquals(findEditPartsForElement.size(), 1);
		StructuredSelection selection = new StructuredSelection(findEditPartsForElement);
		monitor.handleWorkbenchPartSelection(editor, selection, true);
		assertEquals(activeContext.getAllElements().size(), 8);
		assertExists(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book", "ecore");
	}

	private void subtestChangeName(TransactionalEditingDomain domain) {
		EClass book = (EClass) ((Node) diagramResource.getContents().get(0).eContents().get(0)).getElement();
		Command changeName = SetCommand.create(domain, book, EcorePackage.Literals.ENAMED_ELEMENT__NAME, "Livre");
		domain.getCommandStack().execute(changeName);

		assertEquals(activeContext.getAllElements().size(), 8);
		assertEquals(book.getName(), "Livre"); //$NON-NLS-1$
		assertExists(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Livre", "ecore");
		assertNotExists(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book");

		domain.getCommandStack().undo();

		assertEquals(activeContext.getAllElements().size(), 8);
		assertNotExists(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Livre");
		assertExists(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book", "ecore");
	}

	private void subTestDiagramOpen(IInteractionContext activeContext) {
		//Ensure that we're getting the right context types for everything
		assertEquals(activeContext.getAllElements().size(), 7);
		assertExists(activeContext, "/", "resource");
		assertExists(activeContext, "/org.eclipse.mylyn.modeling.tests.ecorediagram", "resource");
		assertExists(activeContext, "/org.eclipse.mylyn.modeling.tests.ecorediagram/model", "resource");
		assertExists(activeContext, "/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore", "resource");
		assertExists(activeContext, "", "java");
		assertExists(activeContext, "=org.eclipse.mylyn.modeling.tests.ecorediagram", "java");
		assertExists(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#/", "ecore");
		IInteractionElement element = ContextCore.getContextManager().getElement(
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecorediag#//Diagram");
		assertNotNull(element);
		assertFalse(element.getInterest().isInteresting());
		assertNotExists(activeContext,
				"platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Diagram");
	}

	public static void assertExists(IInteractionContext activeContext, String id, String type) {
		boolean found = false;
		for (IInteractionElement elem : activeContext.getAllElements()) {
			if (elem.getHandleIdentifier().equals(id)) {
				assertEquals(elem.getContentType(), type);
				found = true;
			}
			assertTrue(elem.getInterest().isInteresting());
		}
		assertTrue("Couldn't find match for: " + id + " of " + type, found);
	}

	public static void assertNotExists(IInteractionContext activeContext, String id) {
		boolean found = false;
		for (IInteractionElement elem : activeContext.getAllElements()) {
			if (elem.getHandleIdentifier().equals(id)) {
				found = true;
			}
		}
		assertFalse("Found match for: " + id, found);
	}

	public static void printContext(IInteractionContext activeContext) {
		//sure diagnostics already exist somewhere, too lazy to find it..
		for (IInteractionElement elem : activeContext.getAllElements()) {
			System.err.println(elem + " " + elem.getContentType());
		}
	}

	//TODO the functionality works in actual model, but we need don't ahve a simple way to select diagram elements
	/**
	 * private void subtestRemove(TransactionalEditingDomain domain) { printContext(activeContext); Node book = (Node)
	 * diagramResource.getContents().get(0).eContents().get(0); Command remove = RemoveCommand.create(domain, book);
	 * domain.getCommandStack().execute(remove); editor.setInput(editor.getEditorInput()); printContext(activeContext);
	 * editor.doSave(new NullProgressMonitor()); printContext(activeContext);
	 * assertEquals(activeContext.getAllElements().size(), 7); assertNotExists(activeContext,
	 * "platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book");
	 * domain.getCommandStack().undo(); editor.doSave(new NullProgressMonitor());
	 * assertEquals(activeContext.getAllElements().size(), 8); assertExists(activeContext,
	 * "platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book", "ecore"); }
	 * private void subtestAdd(TransactionalEditingDomain domain) { EObject rootPackage = ((Diagram)
	 * diagramResource.getContents().get(0)).getElement(); assertTrue("Unexpected type " + rootPackage.eClass(),
	 * rootPackage instanceof EPackage); EClass another = EcoreFactory.eINSTANCE.createEClass();
	 * another.setName("AnotherBook"); Command add = AddCommand.create(domain, rootPackage,
	 * EcorePackage.Literals.EPACKAGE__ECLASSIFIERS, another); domain.getCommandStack().execute(add);
	 * assertEquals(activeContext.getAllElements().size(), 9); EClass anotherBook = (EClass) ((Node)
	 * diagramResource.getContents().get(0).eContents().get(0)).getElement(); assertEquals(anotherBook.getName(),
	 * "AnotherBook"); //$NON-NLS-1$ assertExists(activeContext,
	 * "platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//AnotherBook", "ecore");
	 * domain.getCommandStack().undo(); assertEquals(activeContext.getAllElements().size(), 8);
	 * assertNotExists(activeContext,
	 * "platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//AnotherBook"); }
	 **/
}
