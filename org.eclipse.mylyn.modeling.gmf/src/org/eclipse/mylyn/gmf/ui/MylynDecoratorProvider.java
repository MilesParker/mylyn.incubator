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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.CreateDecoratorsOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecorator;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.ContextChangeEvent;
import org.eclipse.mylyn.context.core.ContextChangeEvent.ContextChangeKind;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.emf.context.DomainAdaptedStructureBridge;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public abstract class MylynDecoratorProvider extends AbstractProvider implements
		IDecoratorProvider {

	public static final String MYLYN_MARKER = "mylyn-marker";

	public static final String MYLYN_DETAIL = "mylyn-detail";

	public static final String MYLYN_INTERESTING = "mylyn-interesting";

	public static final String MYLYN_BORING = "mylyn-boring";

	private Collection<EObject> interestingParts;

	private Collection<MylynDecorator> decorators;

	private DomainAdaptedStructureBridge structure;

	AbstractContextListener contextListenerAdapter = new AbstractContextListener() {
		public void contextChanged(ContextChangeEvent event) {
			MylynDecoratorProvider.this.contextChanged(event);
		}
	};
	
	public MylynDecoratorProvider() {
		ContextCore.getContextManager().addListener(contextListenerAdapter);
		decorators = new HashSet<MylynDecorator>();
	}

	public boolean provides(IOperation operation) {
		if (operation instanceof CreateDecoratorsOperation) {
			CreateDecoratorsOperation cdo = (CreateDecoratorsOperation) operation;
			IDecoratorTarget target = cdo.getDecoratorTarget();
			View view = (View) target.getAdapter(View.class);
			return getStructure().acceptsObject(view.getElement());
		}
		return false;
	}

	public void createDecorators(IDecoratorTarget target) {
		IGraphicalEditPart targetPart = (IGraphicalEditPart) target
				.getAdapter(IGraphicalEditPart.class);
		targetPart.addEditPartListener(new EditPartListener.Stub() {
			@Override
			public void partDeactivated(EditPart editpart) {
			}
			@Override
			public void removingChild(EditPart child, int index) {
			}
		});
		Object model = targetPart.getModel();
		if (model instanceof View) {
			model = ((View) model).getElement();
		}
		if (getStructure().acceptsObject(model)) {
			decorators.add(new MylynDecorator(this, target,
					(EObject) getStructure().getDomainObject(model)));
		}
		updateInterestDecorators(ContextCore.getContextManager()
				.getActiveContext());
	}

	public void contextChanged(ContextChangeEvent event) {
		if (event.getEventKind() != ContextChangeKind.DEACTIVATED
				&& event.getContext() != null) {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			updateInterestDecorators(event.getContext());
			if (activeWorkbenchWindow != null) {
				IEditorReference[] editorReferences = activeWorkbenchWindow
						.getActivePage().getEditorReferences();
				for (IEditorReference reference : editorReferences) {
					IEditorPart editor = reference.getEditor(false);
					if (editor instanceof DiagramDocumentEditor) {
						DiagramDocumentEditor de = (DiagramDocumentEditor) editor;
						de.getDiagramEditPart().getRoot().refresh();
					}
				}
			}
		} else {
			clearDecorators();
		}

	}

	public void removeDecorator(IDecorator decorator) {
		decorators.remove(decorator);
	}
	
	private void clearDecorators() {
		decorators = new ArrayList<MylynDecorator>();
	}

	private void updateInterestDecorators(IInteractionContext context) {
		List<IInteractionElement> elements = context.getAllElements();
		interestingParts = new HashSet<EObject>();
		Collection<IInteractionElement> interestingElems = new HashSet<IInteractionElement>();
		Map<Resource, List<IInteractionElement>> resourceInterests = new HashMap<Resource, List<IInteractionElement>>();
		for (IInteractionElement element : elements) {
			if (element.getInterest().isInteresting()
					&& element.getContentType().equals(
							getContentType())) {
				interestingElems.add(element);
			}
		}

		for (IInteractionElement interaction : interestingElems) {
			Object objectForHandle = structure.getObjectForHandle(interaction
					.getHandleIdentifier());
			if (objectForHandle instanceof EObject) {
				interestingParts.add((EObject) objectForHandle);
			}
		}
		// TODO O[mn], use hashset comparison in case of performance issues
		for (MylynDecorator decorator : decorators) {
			boolean interesting = false;
			for (EObject interestingObject : interestingParts) {
				if (decorator.getModel() instanceof EObject) {
					EObject decoratedModel = (EObject) decorator.getModel();
					if (EcoreUtil.equals(decoratedModel, interestingObject)) {
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
	}

	public DomainAdaptedStructureBridge getStructure() {
		if (structure == null) {
			structure = (DomainAdaptedStructureBridge) ContextCore
					.getStructureBridge(getContentType());
		}
		return structure;
	}

	public abstract String getContentType();
}
