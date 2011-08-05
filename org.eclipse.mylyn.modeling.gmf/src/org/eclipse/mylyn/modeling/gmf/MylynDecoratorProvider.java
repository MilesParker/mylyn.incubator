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

package org.eclipse.mylyn.modeling.gmf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.CreateDecoratorsOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.ContextChangeEvent;
import org.eclipse.mylyn.context.core.ContextChangeEvent.ContextChangeKind;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.modeling.context.DomainAdaptedStructureBridge;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;
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

	private Map<String, MylynDecorator> decoratorForModel;

	private DomainAdaptedStructureBridge structure;

	AbstractContextListener contextListenerAdapter = new AbstractContextListener() {
		public void contextChanged(ContextChangeEvent event) {
			MylynDecoratorProvider.this.contextChanged(event);
		}
	};

	public MylynDecoratorProvider() {
		ContextCore.getContextManager().addListener(contextListenerAdapter);
		decoratorForModel = new HashMap<String, MylynDecorator>();
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
		// targetPart.addEditPartListener(new EditPartListener.Stub() {
		// @Override
		// public void partDeactivated(EditPart editpart) {
		// }
		//
		// @Override
		// public void removingChild(EditPart child, int index) {
		// }
		// });
		Object model = targetPart.getModel();
		if (model instanceof View) {
			model = ((View) model).getElement();
		}
		if (getStructure().acceptsObject(model)) {
			EObject domainObject = (EObject) getStructure().getDomainObject(
					model);
			MylynDecorator mylynDecorator = new MylynDecorator(this, target,
					domainObject);
//			target.installDecorator(MYLYN_DETAIL, mylynDecorator);
			decoratorForModel.put(structure.getHandleIdentifier(domainObject),
					mylynDecorator);
			mylynDecorator.setInteresting(false);
		}
	}

	private void refreshEditors() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IEditorReference[] editorReferences = activeWorkbenchWindow
					.getActivePage().getEditorReferences();
			for (IEditorReference reference : editorReferences) {
				IEditorPart editor = reference.getEditor(false);
				if (getDomainUIBridge().acceptsEditor(editor)) {
					//TODO we can't really assume that this is a diagram editor
					DiagramEditor de = (DiagramEditor) editor;
					de.getDiagramEditPart().getRoot().refresh();
				}
			}
		}
	}

	private void clearDecorators() {
		for (Entry<String, MylynDecorator> entry : decoratorForModel.entrySet()) {
			entry.getValue().deactivate();
		}
//		decoratorForModel.clear();
	}

	private void activateDecorators() {
		for (Entry<String, MylynDecorator> entry : decoratorForModel.entrySet()) {
			entry.getValue().activate();
		}
//		decoratorForModel.clear();
	}
	
	public void contextChanged(ContextChangeEvent event) {
		if (event.getEventKind() == ContextChangeKind.ACTIVATED) {
			for (IInteractionElement element : event.getContext().getAllElements()) {
				if (element.getContentType().equals(
						getDomainUIBridge().getContentType())) {
					MylynDecorator mylynDecorator = decoratorForModel
							.get(element.getHandleIdentifier());
					if (mylynDecorator != null) {
						mylynDecorator.setInteresting(element.getInterest()
								.isInteresting());
//						mylynDecorator.activate();
//						mylynDecorator.getEditPart().refresh();
					}
				}
			}
			activateDecorators();
//			for (Entry<String, MylynDecorator> entry : decoratorForModel
//					.entrySet()) {
//				entry.getValue();
//			}
		} else if (event.getEventKind() == ContextChangeKind.DEACTIVATED){
			clearDecorators();
		} else {
			List<IInteractionElement> elements = event.getElements();
			for (IInteractionElement element : elements) {
				if (element.getContentType().equals(
						getDomainUIBridge().getContentType())) {
					MylynDecorator mylynDecorator = decoratorForModel
							.get(element.getHandleIdentifier());
					mylynDecorator.setInteresting(element.getInterest()
							.isInteresting());
//					mylynDecorator.getEditPart().refresh();
					mylynDecorator.refresh();
				}
			}
		}
//		refreshEditors();
	}

	public abstract IModelUIProvider getDomainUIBridge();

	public DomainAdaptedStructureBridge getStructure() {
		if (structure == null) {
			structure = (DomainAdaptedStructureBridge) ContextCore
					.getStructureBridge(getDomainUIBridge().getContentType());
		}
		return structure;
	}
}
