/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.tasks.tests.web;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Mik Kersten
 */
public class AllFragileWebTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.mylyn.tests");
		//$JUnit-BEGIN$
		suite.addTest(LiveWebConnectorTemplatesTest.suite());
		//$JUnit-END$
		return suite;
	}

}
