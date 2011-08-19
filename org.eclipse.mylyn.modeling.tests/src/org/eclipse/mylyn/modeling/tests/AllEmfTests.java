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
import org.eclipse.mylyn.modeling.context.BasicEmfResourceTest;
import org.eclipse.mylyn.modeling.context.EmfStructureBridgeTest;
import org.eclipse.mylyn.modeling.ui.EcoreDiagramEditorUiBridgeTest;
import org.eclipse.mylyn.modeling.ui.PapyrusDiagramEditorUiBridgeTest;

/**
 * @author Benjamin Muskalla
 * @author Miles Parker
 */
public class AllEmfTests {

	public static Test suite() {
		ContextTestUtil.triggerContextUiLazyStart();

		TestSuite suite = new TestSuite(AllEmfTests.class.getName());
		suite.addTestSuite(BasicEmfResourceTest.class);
		suite.addTestSuite(EmfStructureBridgeTest.class);
//		suite.addTestSuite(EMFUIBridgeTest.class);
		suite.addTestSuite(EcoreDiagramEditorUiBridgeTest.class);
		suite.addTestSuite(PapyrusDiagramEditorUiBridgeTest.class);

		return suite;
	}

}
