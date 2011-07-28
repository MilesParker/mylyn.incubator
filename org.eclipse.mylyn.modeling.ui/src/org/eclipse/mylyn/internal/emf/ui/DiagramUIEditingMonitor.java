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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.ui.AbstractContextUiBridge;
import org.eclipse.mylyn.internal.context.ui.ContextUiPlugin;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author milesparker
 */
public class DiagramUIEditingMonitor extends AbstractUserInteractionMonitor {

	private final AbstractContextStructureBridge structure;

	private AbstractContextUiBridge ui;

	public DiagramUIEditingMonitor(AbstractContextStructureBridge structure, AbstractContextUiBridge ui) {
		super();
		this.structure = structure;
		this.ui = ui;
	}

	@Override
	public void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection, boolean contributeToContext) {
		ui = ContextUiPlugin.getDefault().getUiBridge(structure.getContentType());

		if (part instanceof IEditorPart && ui.acceptsEditor((IEditorPart) part)
				&& selection instanceof IStructuredSelection) {
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
