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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.tests.support.ContextTestUtil;
import org.eclipse.mylyn.internal.resources.ui.ResourceInteractionMonitor;
import org.eclipse.mylyn.internal.resources.ui.ResourceStructureBridge;
import org.eclipse.mylyn.internal.resources.ui.ResourcesUiBridgePlugin;
import org.eclipse.mylyn.internal.resources.ui.ResourcesUiPreferenceInitializer;

/**
 * @author Miles Parker
 */
public class BasicEmfResourceTest extends AbstractEmfContextTest {

	protected ResourceInteractionMonitor resmonitor = new ResourceInteractionMonitor();

	protected ResourceStructureBridge resourceBridge = new ResourceStructureBridge();

	@Override
	protected void setUp() throws Exception {
		// ignore
		super.setUp();
		ResourcesUiBridgePlugin.getInterestUpdater().setSyncExec(true);

		ContextTestUtil.triggerContextUiLazyStart();
		// disable ResourceModifiedDateExclusionStrategy
		ResourcesUiBridgePlugin.getDefault()
				.getPreferenceStore()
				.setValue(ResourcesUiPreferenceInitializer.PREF_MODIFIED_DATE_EXCLUSIONS, false);
	}

	public void testResourceSelect() throws CoreException {
		ContextCore.getContextManager().setContextCapturePaused(true);
		IFile file = getEmfProject().getProject().getFile("model/library.ecore");
		assertTrue(file.exists());

		IInteractionElement element = ContextCore.getContextManager().getElement(
				resourceBridge.getHandleIdentifier(file));
		assertNotNull(element);
		assertNotNull(element.getInterest());
		assertFalse(element.getInterest().isInteresting());
		ContextCore.getContextManager().setContextCapturePaused(false);

		PackageExplorerPart pe = PackageExplorerPart.openInActivePerspective();
		resmonitor.selectionChanged(pe, new StructuredSelection(file));
		element = ContextCore.getContextManager().getElement(resourceBridge.getHandleIdentifier(file));
		assertTrue(element.getInterest().isInteresting());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ResourcesUiBridgePlugin.getInterestUpdater().setSyncExec(false);
		// re-enable ResourceModifiedDateExclusionStrategy
		ResourcesUiBridgePlugin.getDefault()
				.getPreferenceStore()
				.setValue(ResourcesUiPreferenceInitializer.PREF_MODIFIED_DATE_EXCLUSIONS, true);
	}

}
