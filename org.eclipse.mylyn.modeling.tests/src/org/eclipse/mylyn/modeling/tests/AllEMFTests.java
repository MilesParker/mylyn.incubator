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

package org.eclipse.mylyn.modeling.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.mylyn.context.tests.support.ContextTestUtil;
import org.eclipse.mylyn.modeling.context.BasicEMFResourceTest;
import org.eclipse.mylyn.modeling.context.EMFStructureBridgeTest;
import org.eclipse.mylyn.modeling.ui.EcoreDiagramEditorUIBridgeTest;
import org.eclipse.mylyn.modeling.ui.PapyrusDiagramEditorUIBridgeTest;

/**
 * @author Benjamin Muskalla
 * @author Miles Parker
 */
public class AllEMFTests {

	public static Test suite() {
		ContextTestUtil.triggerContextUiLazyStart();

		TestSuite suite = new TestSuite(AllEMFTests.class.getName());
		suite.addTestSuite(BasicEMFResourceTest.class);
		suite.addTestSuite(EMFStructureBridgeTest.class);
//		suite.addTestSuite(EMFUIBridgeTest.class);
		suite.addTestSuite(EcoreDiagramEditorUIBridgeTest.class);
		suite.addTestSuite(PapyrusDiagramEditorUIBridgeTest.class);

		return suite;
	}

}
