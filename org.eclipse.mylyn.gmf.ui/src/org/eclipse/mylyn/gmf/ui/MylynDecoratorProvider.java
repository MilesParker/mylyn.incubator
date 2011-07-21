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

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.CreateDecoratorsOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.mylyn.context.core.ContextChangeEvent;
import org.eclipse.mylyn.context.core.ContextChangeEvent.ContextChangeKind;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IContextListener;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.emf.context.EMFStructureBridge;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

public class MylynDecoratorProvider extends AbstractProvider implements
		IDecoratorProvider, IContextListener {

	public static final String MYLYN_MARKER = "mylyn-marker";

	public static final String MYLYN_DETAIL = "mylyn-detail";

	public static final String MYLYN_INTERESTING = "mylyn-interesting";

	public static final String MYLYN_BORING = "mylyn-boring";

	private Collection<EObject> interestingParts;

	private Collection<EObject> boringParts;

	private Collection<MylynDecorator> decorators;

	private EMFStructureBridge structure = (EMFStructureBridge) ContextCore
			.getStructureBridge(EMFStructureBridge.EMF_CONTENT_TYPE);;

	public MylynDecoratorProvider() {
		ContextCore.getContextManager().addListener(this);
		decorators = new HashSet<MylynDecorator>();
	}

	public boolean provides(IOperation operation) {
		if (operation instanceof CreateDecoratorsOperation) {
			CreateDecoratorsOperation cdo = (CreateDecoratorsOperation) operation;
			IDecoratorTarget target = cdo.getDecoratorTarget();
			View view = (View) target.getAdapter(View.class);
			return structure.acceptsObject(view);
		}
		return false;
	}

	public void createDecorators(IDecoratorTarget target) {
		IGraphicalEditPart targetPart = (IGraphicalEditPart) target
				.getAdapter(IGraphicalEditPart.class);
		Object model = targetPart.getModel();
		if (model instanceof View) {
			model = ((View) model).getElement();
		}
		if (structure.acceptsObject(model)) {
			decorators.add(new MylynDecorator(this, target, structure
					.getDomainObject(model)));
		}
		updateInterestDecorators(ContextCore.getContextManager()
				.getActiveContext());
	}

	@Override
	public void contextChanged(ContextChangeEvent event) {
		if (event.getEventKind() != ContextChangeKind.DEACTIVATED) {
			updateInterestDecorators(event.getContext());
			IEditorReference[] editorReferences = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getEditorReferences();
			for (IEditorReference reference : editorReferences) {
				IEditorPart editor = reference.getEditor(false);
				if (editor instanceof DiagramDocumentEditor) {
					DiagramDocumentEditor de = (DiagramDocumentEditor) editor;
					de.getDiagramEditPart().getRoot().refresh();
				}
			}

		}
	}

	private void updateInterestDecorators(IInteractionContext context) {
		List<IInteractionElement> elements = context.getAllElements();
		interestingParts = new HashSet<EObject>();
		EMFStructureBridge structure = (EMFStructureBridge) ContextCore
				.getStructureBridge(EMFStructureBridge.EMF_CONTENT_TYPE);
		Collection<IInteractionElement> interestingElems = new HashSet<IInteractionElement>();
		Map<Resource, List<IInteractionElement>> resourceInterests = new HashMap<Resource, List<IInteractionElement>>();
		for (IInteractionElement element : elements) {
			if (element.getInterest().isInteresting()
					&& element.getContentType().equals(
							EMFStructureBridge.EMF_CONTENT_TYPE)) {
				interestingElems.add(element);
			}
		}

		for (IInteractionElement interaction : interestingElems) {
			Object objectForHandle = structure.getObjectForHandle(interaction
					.getHandleIdentifier());
			if (objectForHandle instanceof EClassifier) {
				EClassifier eObject = (EClassifier) objectForHandle;
				interestingParts.add(eObject);
			}
		}
		for (MylynDecorator decorator : decorators) {
			boolean interesting = false;
			for (EObject object : interestingParts) {
				if (object instanceof EClassifier
						&& decorator.getModel() instanceof EClassifier) {
					if (((EClassifier) object).getClassifierID() == ((EClassifier) decorator
							.getModel()).getClassifierID()) {
						interesting = true;
						break;
					}
				}
			}
			if (interesting) {
				decorator.setInteresting(true);
			} else {
				decorator.setInteresting(false);
			}
			decorator.refresh();
		}
		// editor.getDiagramGraphicalViewer().getRootEditPart().refresh();
	}
}
