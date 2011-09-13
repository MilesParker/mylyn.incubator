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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.mylyn.commons.sdk.util.ResourceTestUtil;
import org.eclipse.mylyn.modeling.tests.WorkspaceSetupHelper;

/**
 * @author Miles Parker
 */
public class AbstractEmfContextTest extends AbstractModelingContextTest {

	IJavaProject emfProject;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		//ecore diagram project is used for this test, even for non-diagram cases.
		emfProject = WorkspaceSetupHelper.createJavaPluginProjectFromZip(
				"org.eclipse.mylyn.modeling.tests.ecorediagram", "ecorediagram.zip");
		emfProject.open(new NullProgressMonitor());
	}

	@Override
	public void testSetup() {
		assertTrue(emfProject != null);
		assertTrue(emfProject.isOpen());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ResourceTestUtil.deleteProject(emfProject.getProject());
	}

	public IJavaProject getEmfProject() {
		return emfProject;
	}

}
