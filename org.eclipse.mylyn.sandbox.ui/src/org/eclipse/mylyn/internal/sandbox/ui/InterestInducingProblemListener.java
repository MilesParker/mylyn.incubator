/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.IProblemChangedListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.internal.java.ui.JavaStructureBridge;
import org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin;

/**
 * @author Mik Kersten
 * @since 3.0
 */
public class InterestInducingProblemListener implements IProblemChangedListener, IPropertyChangeListener {

	public static final String PREDICTED_INTEREST_ERRORS = "org.eclipse.mylyn.java.ui.interest.prediction.errors";

	// TODO: consider getting rid of this
	private final JavaStructureBridge javaStructureBridge = new JavaStructureBridge();

	public void problemsChanged(IResource[] changedResources, boolean isMarkerChange) {
		try {
			if (!ContextCore.getContextManager().isContextActive()) {
				return;
			} else {
				for (IResource resource : changedResources) {
					if (resource instanceof IFile) {
						IJavaElement javaElement = (IJavaElement) resource.getAdapter(IJavaElement.class);
						if (javaElement != null) {
							IInteractionElement element = ContextCore.getContextManager().getElement(
									javaElement.getHandleIdentifier());
							if (!javaStructureBridge.containsProblem(element)) {
								((InteractionContextManager) ContextCore.getContextManager()).removeErrorPredictedInterest(
										element.getHandleIdentifier(), JavaStructureBridge.CONTENT_TYPE, true);
							} else {
								((InteractionContextManager) ContextCore.getContextManager()).addErrorPredictedInterest(
										element.getHandleIdentifier(), JavaStructureBridge.CONTENT_TYPE, true);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, JavaUiBridgePlugin.ID_PLUGIN, "Could not update marker change",
					e));
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (PREDICTED_INTEREST_ERRORS.equals(event.getProperty())) {
			if (JavaUiBridgePlugin.getDefault().getPreferenceStore().getBoolean(PREDICTED_INTEREST_ERRORS)) {
				enable();
			} else {
				disable();
			}
		}
	}

	public void enable() {
		JavaPlugin.getDefault().getProblemMarkerManager().addListener(this);
	}

	public void disable() {
		JavaPlugin.getDefault().getProblemMarkerManager().removeListener(this);
	}
}
