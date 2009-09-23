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

package org.eclipse.mylyn.tasks.tests.peformance;

import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.tests.connector.MockTask;
import org.eclipse.test.performance.PerformanceTestCase;

/**
 * @author Steffen Pingel
 */
public class TaskContainerPerformanceTest extends PerformanceTestCase {

	int counter;

	private void addChildren(AbstractTask parent, int[] childCount, int depth) {
		for (int i = 0; i < childCount[depth]; i++) {
			MockTask task = new MockTask("task", ++counter + "");
			parent.internalAddChild(task);
			if (depth < childCount.length - 1) {
				addChildren(task, childCount, depth + 1);
			}
		}
	}

	public void testContains() {
		MockTask task = new MockTask(++counter + "");
		addChildren(task, new int[] { 1000, 10, 2 }, 0);

		for (int i = 0; i < 10; i++) {
			startMeasuring();
			task.contains("handle");
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}

}
