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

package org.eclipse.mylyn.gmf.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.ShapeStyle;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.ContextChangeEvent;
import org.eclipse.mylyn.context.core.ContextChangeEvent.ContextChangeKind;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.emf.context.EMFStructureBridge;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

public class GMFEditorContextListener extends AbstractContextListener {
	@Override
	public void contextChanged(ContextChangeEvent event) {
		if (event.getEventKind() != ContextChangeKind.DEACTIVATED) {
			EMFStructureBridge structure = (EMFStructureBridge) ContextCore.getStructureBridge(EMFStructureBridge.EMF_CONTENT_TYPE);
			List<IInteractionElement> elements = event.getContext().getAllElements();
			Collection<IInteractionElement> interestingElems = new HashSet<IInteractionElement>();
			Map<Resource, List<IInteractionElement>> resourceInterests = new HashMap<Resource, List<IInteractionElement>>();
			for (IInteractionElement element : elements) {
				if (element.getInterest().isInteresting()
						&& element.getContentType().equals(EMFStructureBridge.EMF_CONTENT_TYPE)) {
//					Resource res = structure.getUniqueResourceForHandle(element.getHandleIdentifier());
//					if (res != null) {
//						List<IInteractionElement> resElems = resourceInterests.get(res);
//						if (resElems == null) {
//							resElems = new ArrayList<IInteractionElement>();
//							resourceInterests.put(res, resElems);
//						}
//						resElems.add(element);
//					}
					interestingElems.add(element);
				}
			}

			IEditorReference[] editorReferences = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.getEditorReferences();
			for (IEditorReference reference : editorReferences) {
				IEditorPart editor = reference.getEditor(false);
				if (editor instanceof DiagramDocumentEditor) {
					updateDiagram((DiagramDocumentEditor) editor, interestingElems);
				}
			}
		}
	}

	private void updateDiagram(DiagramDocumentEditor editor, Collection<IInteractionElement> interestingElems) {
		EMFStructureBridge structure = (EMFStructureBridge) ContextCore.getStructureBridge(EMFStructureBridge.EMF_CONTENT_TYPE);
		Collection<Node> interestingParts = new HashSet<Node>();
//		for (IInteractionElement interaction : interestingElems) {
//			interaction.getInterest();
//			Object objectForHandle = structure.getObjectForHandle(interaction.getHandleIdentifier());
//			if (objectForHandle instanceof EObject) {
//				//TODO do we ever have more than one?
//				List elements = editor.getDiagramGraphicalViewer().findEditPartsForElement(
//						EMFCoreUtil.getProxyID((EObject) objectForHandle), EClassEditPart.class);
//				interestingParts.addAll(elements);
//			}
//		}
		Collection<Node> allParts = new HashSet<Node>();
		Collection<Node> boringParts = new HashSet<Node>(allParts);
		Collection registry = editor.getDiagramGraphicalViewer().getEditPartRegistry().entrySet();
		for (Object object : registry) {
			Object value = ((Entry) object).getValue();
			if (value instanceof INodeEditPart) {
				INodeEditPart editPart = (INodeEditPart) value;
				if (editPart.getModel() instanceof Node) {
					Node node = (Node) editPart.getModel();
					EClass editClass = (EClass) node.getElement();
					boolean interesting = false;
					for (IInteractionElement interaction : interestingElems) {
						Object objectForHandle = structure.getObjectForHandle(interaction.getHandleIdentifier());
						if (objectForHandle instanceof EClassifier) {
							EClassifier eObject = (EClassifier) objectForHandle;
							if (editClass.getClassifierID() == eObject.getClassifierID()) {
								interesting = true;
								break;
							}
							//TODO do we ever have more than one?
//						List elements = editor.getDiagramGraphicalViewer().findEditPartsForElement(
//								EMFCoreUtil.getProxyID((EObject) objectForHandle), EClassEditPart.class);
//						interestingParts.addAll(elements);
						}
					}
					if (interesting) {
						interestingParts.add(node);
					} else {
						boringParts.add(node);
					}
				}
			}
		}
//		boringParts.removeAll(interestingParts);
		for (Node boringPart : boringParts) {
			markBoring(editor, boringPart);
		}
		for (Node interestingPart : interestingParts) {
			markInteresting(editor, interestingPart);
		}
		editor.getDiagramGraphicalViewer().getRootEditPart().refresh();
	}

	private void markInteresting(DiagramDocumentEditor editor, Node interestingPart) {
		for (Object object : interestingPart.getStyles()) {
			if (object instanceof ShapeStyle) {
				ShapeStyle s = (ShapeStyle) object;
				Command setVisible = SetCommand.create(editor.getEditingDomain(), s,
						NotationPackage.Literals.FILL_STYLE__FILL_COLOR, 100);
				editor.getEditingDomain().getCommandStack().execute(setVisible);
			}
		}
	}

	private void markBoring(DiagramDocumentEditor editor, Node boringPart) {
		for (Object object : boringPart.getStyles()) {
			if (object instanceof ShapeStyle) {
				ShapeStyle s = (ShapeStyle) object;
				Command setVisible = SetCommand.create(editor.getEditingDomain(), s,
						NotationPackage.Literals.FILL_STYLE__FILL_COLOR, 100000);
				editor.getEditingDomain().getCommandStack().execute(setVisible);
			}
		}
	}
}
