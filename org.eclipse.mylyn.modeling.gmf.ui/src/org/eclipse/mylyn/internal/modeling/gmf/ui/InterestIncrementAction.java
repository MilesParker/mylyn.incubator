/*******************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.modeling.gmf.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.ui.actions.AbstractInterestManipulationAction;

/**
 * @author Mik Kersten
 * @author Miles Parker
 */
public class InterestIncrementAction extends AbstractInterestManipulationAction {

	@Override
	protected boolean isIncrement() {
		return true;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		boolean allLandmarked = true;
		for (Object object : structuredSelection.toList()) {
			IInteractionElement node = convertSelectionToInteractionElement(object);
			if (!node.getInterest().isLandmark()) {
				allLandmarked = false;
				break;
			}
		}
		action.setEnabled(!allLandmarked);
	}
}
