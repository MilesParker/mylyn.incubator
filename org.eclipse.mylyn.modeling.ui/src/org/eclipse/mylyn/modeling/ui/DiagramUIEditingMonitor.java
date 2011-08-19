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

package org.eclipse.mylyn.modeling.ui;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Miles Parker
 */
public class DiagramUIEditingMonitor extends AbstractUserInteractionMonitor {

	private final AbstractContextStructureBridge structure;

	private final IModelUIProvider ui;

	public DiagramUIEditingMonitor(AbstractContextStructureBridge structure, IModelUIProvider ui) {
		super();
		this.structure = structure;
		this.ui = ui;
	}

	@Override
	public void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection, boolean contributeToContext) {
		if (ui.acceptsPart(part) && selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (structure.acceptsObject(object)) {
					handleElementSelection(part, object, contributeToContext);
				}
			}
		}
	}

}
