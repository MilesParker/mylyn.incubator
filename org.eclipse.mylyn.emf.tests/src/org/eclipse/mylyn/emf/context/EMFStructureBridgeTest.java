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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class EMFStructureBridgeTest extends AbstractEMFContextTest {

	public void testHandles() throws Exception {
		ResourceSet rs = new ResourceSetImpl();
		Resource resource = rs.getResource(
				URI.createPlatformResourceURI("/org.eclipse.mylyn.emf.tests.library/model/library.ecore", false), true);
		EClass bookClass = (EClass) ((EPackage) resource.getContents().get(0)).getEClassifiers().get(0);
		URI uri = EcoreUtil.getURI(bookClass);
		String fragment = uri.fragment();
		EObject eObject = resource.getEObject("//Book");
		EClass fragmentClass = (EClass) eObject;
		assertTrue(eObject instanceof EClass);
		String handleIdentifier = structureBridge.getHandleIdentifier(eObject);
		Object objectForHandle = structureBridge.getObjectForHandle(handleIdentifier);
		EClass obtainedClass = (EClass) objectForHandle;
		assertTrue("Same eobject", !eObject.equals(objectForHandle));
		assertEquals(fragmentClass.getClassifierID(), obtainedClass.getClassifierID());
	}
}
