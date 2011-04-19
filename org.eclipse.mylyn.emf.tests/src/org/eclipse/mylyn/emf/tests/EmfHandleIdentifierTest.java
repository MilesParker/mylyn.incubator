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

import org.eclipse.mylyn.context.tests.AbstractContextTest;
import org.eclipse.mylyn.internal.emf.ui.EmfStructureBridge;

/**
 * @author Benjamin Muskalla
 */
public class EmfHandleIdentifierTest extends AbstractContextTest {

	private EmfStructureBridge bridge;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		bridge = new EmfStructureBridge();
	}

	public void testHandleIdentifierNull() throws Exception {
		assertEquals(null, bridge.getHandleIdentifier(null));
	}

	public void testHandleIdentifierNonEObject() throws Exception {
		assertEquals(null, bridge.getHandleIdentifier(new Object()));
		assertEquals(null, bridge.getHandleIdentifier(this));
	}

}
