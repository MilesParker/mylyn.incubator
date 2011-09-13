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

package org.eclipse.mylyn.modeling.context;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.mylyn.modeling.emf.ecore.EcoreDomainBridge;

/**
 * @author Miles Parker
 */
public class EmfStructureBridgeTest extends AbstractEmfContextTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		structureModelBridge = new EcoreDomainBridge();
	}

	public void testSimpleHandle() {
		String elementHandle = "platform:/resource/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore#//Book";
		Object objectForHandle = structureModelBridge.getObjectForHandle(elementHandle);
		assertTrue(objectForHandle instanceof EClass);
		assertEquals(((EClass) objectForHandle).getName(), "Book");
//		Resource res = structureBridge.getUniqueResourceForHandle(elementHandle);
//		assertNotNull(res);
	}

	public void testHandles() throws Exception {
		ResourceSet rs = new ResourceSetImpl();
		Resource resource = rs.getResource(URI.createPlatformResourceURI(
				"/org.eclipse.mylyn.modeling.tests.ecorediagram/model/library.ecore", false), true);
		EClass bookClass = (EClass) ((EPackage) resource.getContents().get(0)).getEClassifiers().get(0);
		URI uri = EcoreUtil.getURI(bookClass);
		String fragment = uri.fragment();
		EObject eObject = resource.getEObject("//Book");
		EClass fragmentClass = (EClass) eObject;
		assertTrue(eObject instanceof EClass);
		String handleIdentifier = structureModelBridge.getHandleIdentifier(eObject);
		Object objectForHandle = structureModelBridge.getObjectForHandle(handleIdentifier);
		EClass obtainedClass = (EClass) objectForHandle;
		assertTrue("Same eobject", !eObject.equals(objectForHandle));
		assertEquals(fragmentClass.getClassifierID(), obtainedClass.getClassifierID());
	}

}
