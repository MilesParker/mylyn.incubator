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

package org.eclipse.mylyn.sandbox.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.mylyn.monitor.reports.tests.AllMonitorReportTests;
import org.eclipse.mylyn.monitor.tests.InteractionEventExternalizationTest;
import org.eclipse.mylyn.monitor.tests.InteractionLoggerTest;
import org.eclipse.mylyn.monitor.tests.MonitorPackagingTest;
import org.eclipse.mylyn.monitor.tests.MonitorTest;
import org.eclipse.mylyn.monitor.tests.MultiWindowMonitorTest;
import org.eclipse.mylyn.monitor.tests.StatisticsLoggingTest;
import org.eclipse.mylyn.monitor.usage.tests.AllMonitorUsageTests;
import org.eclipse.mylyn.tasks.tests.web.HtmlDecodeEntityTest;
import org.eclipse.mylyn.tasks.tests.web.NamedPatternTest;
import org.eclipse.mylyn.tasks.tests.web.WebRepositoryConnectorTest;
import org.eclipse.mylyn.tasks.tests.web.WebRepositoryTest;

/**
 * @author Mik Kersten
 */
public class AllSandboxTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.mylyn.sandbox.tests");

		// FIXME re-enable
		//suite.addTestSuite(TaskReportGeneratorTest.class);
		suite.addTestSuite(PredictedErrorInterestTest.class);
		suite.addTestSuite(ActiveHierarchyTest.class);
		// FIXME re-enable
		//suite.addTestSuite(ActiveSearchTest.class);
		suite.addTestSuite(StatisticsReportingTest.class);
		suite.addTestSuite(EclipseTaskRepositoryLinkProviderTest.class);
		suite.addTestSuite(RelationProviderTest.class);

		// web connector tests
		suite.addTestSuite(NamedPatternTest.class);
		suite.addTestSuite(HtmlDecodeEntityTest.class);
		suite.addTestSuite(WebRepositoryTest.class);
		suite.addTestSuite(WebRepositoryConnectorTest.class);

		// monitor tests
		suite.addTest(AllMonitorUsageTests.suite());
		suite.addTest(AllMonitorReportTests.suite());
		suite.addTestSuite(InteractionLoggerTest.class);
		suite.addTestSuite(StatisticsLoggingTest.class);
		suite.addTestSuite(MonitorTest.class);
		suite.addTestSuite(InteractionEventExternalizationTest.class);
		suite.addTestSuite(MonitorPackagingTest.class);
		suite.addTestSuite(MultiWindowMonitorTest.class);

		return suite;
	}
}
