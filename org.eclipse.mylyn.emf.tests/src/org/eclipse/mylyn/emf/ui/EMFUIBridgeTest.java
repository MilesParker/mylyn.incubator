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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.emf.context.AbstractEMFContextTest;
import org.eclipse.mylyn.emf.context.EcoreDiagramBridge;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class EMFUIBridgeTest extends AbstractEMFContextTest {

	public void testModification() throws Exception {
		getEmfProject().open(new NullProgressMonitor());
		IProject project = getEmfProject().getProject();
		IFile file = project.getFile("model/library.ecore");
		assertNotNull(file);

		assertTrue(file.exists());
		FileEditorInput input = new FileEditorInput(file);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		EcoreEditor ed = (EcoreEditor) page.openEditor(input, "org.eclipse.emf.ecore.presentation.EcoreEditorID");

		System.out.println(ContextCore.getContextManager().getActiveContext().getAllElements());

		IInteractionElement element = ContextCore.getContextManager().getElement(
				"platform:/resource/org.eclipse.mylyn.emf.tests.library/model/library.ecore#//Book");

		assertFalse(element.getInterest().isInteresting());

		EPackage p = (EPackage) ed.getEditingDomain().getResourceSet().getResources().get(0).getContents().get(0);

		EClassifier book = p.getEClassifier("Book");

		ed.setSelection(new StructuredSelection(book));

		assertNotNull(element);
		assertNotNull(element.getInterest());

		IInteractionElement element2 = ContextCore.getContextManager().getElement(
				"platform:/resource/org.eclipse.mylyn.emf.tests.library/model/library.ecore#//Book");
		assertTrue(element2.getInterest().isInteresting());

		assertEquals(element2.getContentType(), EcoreDiagramBridge.ECORE_CONTENT_TYPE);
	}
}
