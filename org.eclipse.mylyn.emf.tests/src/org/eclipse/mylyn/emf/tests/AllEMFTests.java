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

package org.eclipse.mylyn.emf.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.mylyn.context.tests.support.ContextTestUtil;
import org.eclipse.mylyn.emf.context.BasicEMFResourceTest;
import org.eclipse.mylyn.emf.context.EMFStructureBridgeTest;
import org.eclipse.mylyn.emf.ui.EMFUIBridgeTest;
import org.eclipse.mylyn.emf.ui.PapyrusDiagramEditorUIBridgeTest;

/**
 * @author Benjamin Muskalla
 */
public class AllEMFTests {

	public static Test suite() {
		ContextTestUtil.triggerContextUiLazyStart();

		TestSuite suite = new TestSuite(AllEMFTests.class.getName());
		suite.addTestSuite(BasicEMFResourceTest.class);
		suite.addTestSuite(EMFStructureBridgeTest.class);
		suite.addTestSuite(EMFUIBridgeTest.class);
		suite.addTestSuite(PapyrusDiagramEditorUIBridgeTest.class);

		return suite;
	}

}
