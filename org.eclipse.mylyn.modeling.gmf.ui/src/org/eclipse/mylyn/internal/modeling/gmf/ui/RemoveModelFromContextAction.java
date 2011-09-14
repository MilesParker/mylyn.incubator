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

package org.eclipse.mylyn.internal.modeling.gmf.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.context.ui.actions.InterestDecrementAction;
import org.eclipse.mylyn.internal.resources.ui.ResourceStructureBridge;
import org.eclipse.mylyn.modeling.emf.EmfStructureBridge;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class RemoveModelFromContextAction extends InterestDecrementAction {

	//We have to shadow selection here see https://bugs.eclipse.org/bugs/show_bug.cgi?id=357544
	ISelection selection;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run(IAction action) {
		super.run(action);
		ISelection sel = getSelection();
		if (sel instanceof StructuredSelection) {
			StructuredSelection ss = (StructuredSelection) sel;
			List selList = ss.toList();
			if (!selList.isEmpty()) {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				for (IEditorReference ref : activePage.getEditorReferences()) {
					//Don't restore, we only want current parts
					IWorkbenchPart part = ref.getPart(false);
					if (part != null) {
						ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
						if (selectionProvider.getSelection() instanceof StructuredSelection) {
							StructuredSelection editorSelection = (StructuredSelection) selectionProvider.getSelection();
							List editorList = editorSelection.toList();
							List matchList = new ArrayList();
							//O^2 but should be ok as selection is typically one or two items.
							for (Object so : selList) {
								EObject sd = getDomainObject(so);
								String sHandle = EmfStructureBridge.getGenericDomainHandleIdentifier(sd,
										ResourceStructureBridge.CONTENT_TYPE);
								for (Object eo : editorList) {
									EObject ed = getDomainObject(eo);
									String eHandle = EmfStructureBridge.getGenericDomainHandleIdentifier(ed,
											ResourceStructureBridge.CONTENT_TYPE);
									if (eHandle != null && eHandle.equals(sHandle)) {
										matchList.add(eo);
									}
								}
							}
							if (!matchList.isEmpty()) {
								ArrayList newList = new ArrayList(editorList);
								newList.removeAll(matchList);
								selectionProvider.setSelection(new StructuredSelection(newList));
							}
						}
					}
				}
			}
		}
	}

	public static EObject getDomainObject(Object o) {
		if (o instanceof View) {
			return ((View) o).getElement();
		} else if (o instanceof EditPart) {
			EObject model = (EObject) ((EditPart) o).getModel();
			if (model instanceof View) {
				return ((View) model).getElement();
			}
			return model;
		}
		return null;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		this.selection = selection;
	}

	public ISelection getSelection() {
		return selection;
	}
}
