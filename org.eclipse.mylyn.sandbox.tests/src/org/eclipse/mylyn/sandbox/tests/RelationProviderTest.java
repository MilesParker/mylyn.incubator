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

package org.eclipse.mylyn.sandbox.tests;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.AbstractRelationProvider;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.java.tests.AbstractJavaContextTest;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public class RelationProviderTest extends AbstractJavaContextTest {

	public void testEdgeReset() throws CoreException, InterruptedException, InvocationTargetException {
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IMethod m1 = type1.createMethod("public void m1() { }", null, true, null);
		IPackageFragment p2 = project.createPackage("p2");

		IType type2 = project.createType(p2, "Type2.java", "public class Type2 { }");
		IMethod m2 = type2.createMethod("void m2() { }", null, true, null);

		assertTrue(m1.exists());
		assertEquals(1, type1.getMethods().length);

		monitor.selectionChanged(part, new StructuredSelection(m1));
		IInteractionElement m1Node = ContextCore.getContextManager().getElement(m1.getHandleIdentifier());
		assertTrue(m1Node.getInterest().isInteresting());
		monitor.selectionChanged(part, new StructuredSelection(m2));
		IInteractionElement m2Node = ContextCore.getContextManager().getElement(m2.getHandleIdentifier());
		manager.processInteractionEvent(mockInterestContribution(m2.getHandleIdentifier(), scaling.getLandmark()));
		assertTrue(m2Node.getInterest().isLandmark());

		AbstractRelationProvider provider = ContextCorePlugin.getDefault()
				.getRelationProviders("java")
				.iterator()
				.next();
		provider.createEdge(m2Node, m1Node.getContentType(), m2.getHandleIdentifier());

		assertEquals(1, m2Node.getRelations().size());

		manager.resetLandmarkRelationshipsOfKind(provider.getId());

		assertEquals(0, m2Node.getRelations().size());
	}

}
