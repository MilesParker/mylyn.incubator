/*******************************************************************************
 * Copyright (c) 2004, 2008 George Lindholm and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     George Lindholm - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.tasks.tests.web;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.web.tasks.WebRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.tests.util.TestTaskDataCollector;

/**
 * @author George Lindholm
 * @author Steffen Pingel
 */
public class HtmlDecodeEntityTest extends TestCase {

	private final IProgressMonitor monitor = new NullProgressMonitor();

	private final TaskRepository repository = new TaskRepository("localhost", "file:///tmp/a");

	private final TestTaskDataCollector collector = new TestTaskDataCollector();

	public void testEntities() {
		assertQuery("1:A quote &quot;", "(\\d+?):(.+)", "A quote \""); // Simple quote
		assertQuery("2:A quote '&quot;'", "(\\d+?):(.+)", "A quote '\"'"); // Simple quote
		assertQuery("3:A quote &quot;&quot; doubled", "({Id}\\d+?):({Description}.+)", "A quote \"\" doubled"); // Double quotes
		assertQuery("4:A quote &quot ;", "(\\d+?):(.+)", "A quote &quot ;"); // Bad entity syntax
		assertQuery("5:A quote & quot;", "(\\d+?):(.+)", "A quote & quot;"); // Bad entity syntax
		assertQuery("6:foo & boo", "(\\d+?):(.+)", "foo & boo"); // Non entity syntax
		assertQuery("7:foo&boo poo", "(\\d+?):(.+)", "foo&boo poo"); // Non entity  syntax
		assertQuery("8:foo&boo ;poo", "(\\d+?):(.+)", "foo&boo ;poo"); // Bad, non entity syntax
		assertQuery("9:foo&boo;poo", "(\\d+?):(.+)", "foo&boo;poo"); // Invalid entity
		assertQuery("10:&#32;", "(\\d+?):(.+)", " "); // HTML decimal entity
		assertQuery("11:&#X20;", "(\\d+?):(.+)", " "); // Hexadecimal entity
	}

	private void assertQuery(final String entity, final String regex, final String expected) {
		collector.results.clear();
		IStatus status = WebRepositoryConnector.performQuery(entity, regex, "", monitor, collector, repository);
		assertEquals(Status.OK_STATUS, status);
		assertEquals(expected, new TaskMapper(collector.results.get(0)).getSummary());
	}

}
