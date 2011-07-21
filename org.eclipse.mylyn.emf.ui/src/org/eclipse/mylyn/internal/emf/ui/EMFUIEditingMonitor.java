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

package org.eclipse.mylyn.internal.emf.ui;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Benjamin Muskalla
 * @author milesparker
 */
public class EMFUIEditingMonitor extends AbstractUserInteractionMonitor {

	@Override
	public void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection, boolean contributeToContext) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof EObject
						|| (object instanceof IAdaptable && ((IAdaptable) object).getAdapter(EClass.class) instanceof EObject)) {
					handleElementSelection(part, object, contributeToContext);
				}//test 3
//				if (selectedObject instanceof IFile) {
//					IFile file = (IFile) selectedObject;
//					if (file.getFileExtension().equals("ecore")) {
//						handleElementSelection(part, selectedObject, contributeToContext);
//					}
//				}
			}

		}
	}
}
