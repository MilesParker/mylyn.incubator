/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Meghan Allen - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.usage.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Meghan Allen
 */
public class AllMonitorUsageTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.mylyn.monitor.ui.usage.tests");
		suite.addTestSuite(DefaultPreferenceConfigTest.class);
		suite.addTestSuite(InteractionEventLoggerTest.class);
		return suite;
	}

}
